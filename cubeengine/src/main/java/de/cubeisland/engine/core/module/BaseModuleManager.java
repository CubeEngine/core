/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.module;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.ModuleAlreadyLoadedException;
import de.cubeisland.engine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.engine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.engine.core.module.exception.CircularDependencyException;
import de.cubeisland.engine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.engine.core.module.exception.InvalidModuleException;
import de.cubeisland.engine.core.module.exception.MissingDependencyException;
import de.cubeisland.engine.core.module.exception.MissingServiceProviderException;
import de.cubeisland.engine.core.module.exception.ModuleDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.module.service.ServiceManager;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.logging.Log;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.core.contract.Contract.expect;
import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.JAR;
import static java.util.Map.Entry;


public abstract class BaseModuleManager implements ModuleManager
{
    private final Log logger;
    protected final Core core;
    private final ModuleLoader loader;
    private final Map<String, Module> modules;
    private final Map<String, ModuleInfo> moduleInfoMap;
    private final Map<Class<? extends Module>, Module> classMap;
    private final CoreModule coreModule;
    private final ServiceManager serviceManager;

    private final Map<String, LinkedList<String>> serviceProviders;

    protected BaseModuleManager(Core core, ServiceManager serviceManager, ModuleLoader loader)
    {
        this.core = core;
        this.logger = core.getLog();
        this.loader = loader;
        this.modules = new LinkedHashMap<>();
        this.moduleInfoMap = new THashMap<>();
        this.classMap = new THashMap<>();
        this.coreModule = new CoreModule();
        this.serviceProviders = new HashMap<>();
        this.coreModule.initialize(core, new ModuleInfo(core), core.getFileManager().getDataPath(), null, null);
        this.serviceManager = serviceManager;
    }

    @Override
    public ServiceManager getServiceManager()
    {
        return this.serviceManager;
    }

    public synchronized Module getModule(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.modules.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T extends Module> T getModule(Class<T> mainClass)
    {
        return (T)this.classMap.get(mainClass);
    }

    public synchronized Collection<Module> getModules()
    {
        return new ArrayList<>(this.modules.values());
    }

    private synchronized ModuleInfo loadModuleInfo(Path modulePath) throws InvalidModuleException, ModuleAlreadyLoadedException
    {
        ModuleInfo info = this.loader.loadModuleInfo(modulePath);
        if (info == null)
        {
            throw new InvalidModuleException("Failed to load the module info for file '" + modulePath.getFileName() + "'!");
        }

        if (this.moduleInfoMap.containsKey(info.getId()))
        {
            throw new ModuleAlreadyLoadedException(info.getName());
        }

        for (String service : info.getProvidedServices())
        {
            this.addService(service, info.getId());
        }

        this.moduleInfoMap.put(info.getId(), info);
        return info;
    }

    private void addService(String service, String module)
    {
        LinkedList<String> providers = this.serviceProviders.get(service);
        if (providers == null)
        {
            providers = new LinkedList<>();
            this.serviceProviders.put(service, providers);
        }
        providers.remove(module);
        providers.addLast(module);
    }

    public synchronized Module loadModule(Path modulePath) throws ModuleException
    {
        expectNotNull(modulePath, "The file must not be null!");
        if (!Files.isRegularFile(modulePath))
        {
            throw new IllegalArgumentException("The given File is does not exist is not a normal file!");
        }
        return this.loadModule(loadModuleInfo(modulePath).getName(), this.moduleInfoMap);
    }

    public synchronized void loadModules(Path directory)
    {
        expectNotNull(directory, "The directory must not be null!");
        expect(Files.isDirectory(directory), "The given File is no directory!");

        Module module;
        ModuleInfo info;
        this.logger.info("Loading modules...");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, JAR))
        {
            for (Path file : directoryStream)
            {
                try
                {
                    info = this.loadModuleInfo(file);
                    module = this.getModule(info.getId());
                    if (module != null)
                    {
                        if (module.getInfo().getVersion().compareTo(info.getVersion()) >= 0)
                        {
                            this.logger.warn("A newer or equal version of the module '" + info.getName() + "' is already loaded!");
                            continue;
                        }
                        else
                        {
                            this.unloadModule(module, true);
                            this.logger.info("A newer version of '{}' will replace the currently loaded version!", info.getName());
                        }
                    }
                    this.moduleInfoMap.put(info.getId(), info);
                }
                catch (ModuleAlreadyLoadedException ignored)
                {}
                catch (InvalidModuleException e)
                {
                    this.logger.error(e, "Failed to load the module from {}!", file);
                }
            }
        }
        catch (IOException ex)
        {
            this.core.getLog().error(ex, "Failed to load modules!");
            return;
        }
        Collection<String> moduleNames = new HashSet<>(this.moduleInfoMap.keySet());
        for (String moduleName : moduleNames)
        {
            try
            {
                this.loadModule(moduleName, this.moduleInfoMap);
            }
            catch (InvalidModuleException ex)
            {
                this.moduleInfoMap.remove(moduleName);
                this.logger.debug(ex, "Failed to load the module '{}'", moduleName);
            }
            catch (ModuleException ex)
            {
                this.moduleInfoMap.remove(moduleName);
                this.logger.error(ex, "Failed to load the module '{}'", moduleName);
            }
        }
        this.logger.info("Finished loading modules!");
    }

    public LinkedList<String> resolveDependencies(String moduleId, Map<String, ModuleInfo> infoMap) throws CircularDependencyException
    {
        LinkedList<String> out = new LinkedList<>();
        this.resolveDependencies0(moduleId, infoMap, new Stack<String>(), out);
        return out;
    }

    private void resolveDependencies0(String moduleId, Map<String, ModuleInfo> infoMap, Stack<String> loadStack, LinkedList<String> out) throws CircularDependencyException
    {
        if (loadStack.contains(moduleId))
        {
            throw new CircularDependencyException(moduleId, loadStack.peek());
        }
        ModuleInfo info = infoMap.get(moduleId);
        if (info == null)
        {
            return;
        }

        loadStack.add(moduleId);

        out.remove(moduleId);
        out.addFirst(moduleId);

        Set<String> soft = new HashSet<>(info.getLoadAfter());
        soft.addAll(info.getSoftDependencies().keySet());
        for (String dep : soft)
        {
            if (infoMap.containsKey(dep))
            {
                this.resolveDependencies0(dep, infoMap, loadStack, out);
            }
        }

        Set<String> strong = new HashSet<>();
        for (String service : info.getServices())
        {
            LinkedList<String> providers = this.serviceProviders.get(service);
            if (providers != null)
            {
                strong.add(providers.getLast());
            }
        }
        strong.addAll(info.getDependencies().keySet());
        for (String dep : strong)
        {
            this.resolveDependencies0(dep, infoMap, loadStack, out);
        }

        loadStack.remove(moduleId);
    }

    protected void verifyDependencies(ModuleInfo info) throws ModuleDependencyException
    {
        Module m;
        Version v;
        for (Entry<String, Version> dep : info.getSoftDependencies().entrySet())
        {
            m = this.modules.get(dep.getKey());
            if (m == null)
            {
                this.logger.debug("The module {} is missing the soft dependency {}...", info.getId(), dep.getKey());
                continue;
            }
            v = dep.getValue();
            if (v.isNewerThan(Version.ZERO) && m.getInfo().getVersion().isOlderThan(v))
            {
                this.logger.warn("The module " + info.getId() + " requested a newer version of " + dep.getKey() + "!");
            }
        }
        for (Entry<String, Version> dep : info.getDependencies().entrySet())
        {
            m = this.modules.get(dep.getKey());
            v = dep.getValue();
            if (m == null)
            {
                throw new MissingDependencyException(dep.getKey());
            }
            else
            {
                if (v.isNewerThan(Version.ZERO) && m.getInfo().getVersion().isOlderThan(v))
                {
                    throw new IncompatibleDependencyException(info.getId(), m.getId(), v, m.getInfo().getVersion());
                }
            }
        }
        for (String service : info.getServices())
        {
            if (!this.serviceProviders.containsKey(service))
            {
                throw new MissingServiceProviderException(info.getId(), service);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private Module loadModule(String id, Map<String, ModuleInfo> infoMap) throws ModuleException
    {
        id = id.toLowerCase(Locale.ENGLISH);
        Module module = this.modules.get(id);
        if (module != null)
        {
            return module;
        }

        if (!infoMap.containsKey(id))
        {
            return null;
        }

        LinkedList<String> loadOrder = this.resolveDependencies(id, infoMap);
        loadOrder.removeAll(this.modules.keySet());

        for (String loadId : loadOrder)
        {
            ModuleInfo info = infoMap.get(loadId);
            this.verifyDependencies(info);
            module = this.loader.loadModule(this.moduleInfoMap.get(loadId));
            this.postModuleLoad(module);
            this.modules.put(module.getId(), module);
            this.classMap.put(module.getClass(), module);
        }

        return module;
    }

    protected void postModuleLoad(Module module)
    {
        Version requiredVersion;
        Module injectedModule;
        Class fieldType;
        Field[] fields = new Field[0];
        try
        {
            fields = module.getClass().getDeclaredFields();
        }
        catch (NoClassDefFoundError ex)
        {
            module.getLog().warn(ex, "Failed to get the fields of the main class");
        }
        for (Field field : fields)
        {
            fieldType = field.getType();
            Inject injectAnnotation = field.getAnnotation(Inject.class);

            if (Module.class.isAssignableFrom(fieldType) && injectAnnotation != null)
            {
                injectedModule = this.classMap.get(fieldType);
                if (injectedModule == null || fieldType == module.getClass())
                {
                    continue;
                }
                requiredVersion = module.getInfo().getSoftDependencies().get(injectedModule.getId());
                if (requiredVersion != null && requiredVersion.isNewerThan(Version.ZERO) && injectedModule.getInfo().getVersion().isOlderThan(requiredVersion))
                {
                    continue;
                }
                field.setAccessible(true);
                try
                {
                    if (field.get(module) == null)
                    {
                        field.set(module, injectedModule);
                    }
                }
                catch (ReflectiveOperationException e)
                {
                    module.getLog().warn("Failed to inject a dependency: {}", injectedModule.getName());
                }
            }
        }
    }

    public synchronized boolean enableModule(Module module)
    {
        boolean result = this.enableModule0(module);
        if (!result)
        {
            module.getLog().error("Module failed to enable, unloading it now.");
            this.unloadModule(module);
        }
        return result;
    }

    protected synchronized boolean enableModule0(Module module)
    {
        module.getLog().info("Enabling version {}...", module.getVersion());
        Profiler.startProfiling("enable-module");
        boolean result = module.enable();
        final long enableTime = Profiler.endProfiling("enable-module", TimeUnit.MICROSECONDS);
        if (result)
        {
            this.core.getEventManager().fireEvent(new ModuleEnabledEvent(this.core, module));
            for (String service : module.getInfo().getProvidedServices())
            {
                this.addService(service, module.getId());
            }
            module.getLog().info("Successfully enabled within {} microseconds!", enableTime);
        }
        return result;
    }

    public synchronized void enableModules()
    {
        List<Module> brokenModules = new ArrayList<>();
        for (Module module : this.modules.values())
        {
            if (!this.enableModule0(module))
            {
                brokenModules.add(module);
                module.getLog().error("Module failed to enable, queued for unloading...");
            }
        }

        for (Module module : brokenModules)
        {
            this.unloadModule(module);
        }
    }

    public synchronized void disableModule(Module module)
    {
        boolean wasEnabled = module.isEnabled();
        if (wasEnabled)
        {
            Profiler.startProfiling("disable-module");
        }

        module.disable();
        this.core.getUserManager().cleanup(module);
        this.core.getEventManager().removeListeners(module);
        this.core.getPermissionManager().removePermissions(module);
        this.core.getTaskManager().clean(module);
        this.core.getCommandManager().removeCommands(module);
        this.core.getApiServer().unregisterApiHandlers(module);

        if (wasEnabled)
        {
            this.core.getEventManager().fireEvent(new ModuleDisabledEvent(this.core, module));
        }

        Iterator<Entry<String, LinkedList<String>>> it = this.serviceProviders.entrySet().iterator();
        while (it.hasNext())
        {
            if (it.next().getValue().remove(module.getId()))
            {
                it.remove();
            }
        }
        this.core.getModuleManager().getServiceManager().unregisterServices(module);
        this.core.getModuleManager().getServiceManager().removeImplementations(module);

        if (wasEnabled)
        {
            module.getLog().info("Module disabled within {} microseconds", Profiler.endProfiling("disable-module", TimeUnit.MICROSECONDS));
        }
    }

    /**
     * Resolves the module that need to unload or reload when unloading given module
     *
     * @param module the module
     * @param willReload true if the module will be reloaded
     * @param modules the collection of modules
     * @param out the list of modules that need to be unloaded
     */
    private void resolveModulesForUnload(Module module, boolean willReload, Collection<Module> modules, LinkedList<Pair<Module, Boolean>> out)
    {
        boolean isServiceProvider = !module.getInfo().getProvidedServices().isEmpty();
        for (Module m : modules)
        {
            if (module == m)
            {
                continue;
            }
            final boolean isSoftDep = m.getInfo().getSoftDependencies().containsKey(module.getId());
            final boolean isDep = isSoftDep || m.getInfo().getDependencies().containsKey(module.getId());
            if (isDep)
            {
                this.resolveModulesForUnload(m, willReload, modules, out);
                out.addLast(new Pair<>(m, willReload || isSoftDep));
            }
            else if (isServiceProvider)
            {
                for (String service : module.getInfo().getProvidedServices())
                {
                    if (m.getInfo().getServices().contains(service))
                    {
                        this.resolveModulesForUnload(m, willReload, modules, out);
                        out.addLast(new Pair<>(m, willReload));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void unloadModule(Module module)
    {
        this.loadModules(this.unloadModule(module, false));
    }

    private synchronized void loadModules(List<ModuleInfo> reloadModules)
    {
        for (ModuleInfo info : reloadModules)
        {
            String id = info.getId();
            try
            {
                this.moduleInfoMap.put(id, info);
                Module m = this.loadModule(id, this.moduleInfoMap);
                this.enableModule(m);
            }
            catch (ModuleException e)
            {
                this.logger.warn(e, "Failed to reload a module upon unloading a different module!");
            }
            finally
            {
                // remove the info again if the module has not been loaded
                if (!this.modules.containsKey(id))
                {
                    this.moduleInfoMap.remove(id);
                }
            }
        }
    }

    private synchronized List<ModuleInfo> unloadModule(final Module module, boolean reload)
    {
        if (!this.modules.containsKey(module.getId()))
        {
            return null;
        }

        LinkedList<Pair<Module, Boolean>> unloadModules = new LinkedList<>();

        this.resolveModulesForUnload(module, reload, new THashSet<>(this.modules.values()), unloadModules);
        unloadModules.addLast(new Pair<>(module, false));

        List<ModuleInfo> reloadModules = new ArrayList<>();

        for (Pair<Module, Boolean> entry : unloadModules)
        {
            if (entry.getRight() == null) // look for services
            {
                for (String service : entry.getLeft().getInfo().getServices())
                {
                    LinkedList<String> providers = this.serviceProviders.get(service);
                    if (providers == null || providers.isEmpty())
                    {
                        continue;
                    }
                    reloadModules.add(entry.getLeft().getInfo());
                }

            }
            else if (entry.getRight())
            {
                reloadModules.add(entry.getLeft().getInfo());
            }
        }


        for (Pair<Module, Boolean> pair : unloadModules)
        {
            this.unloadModule0(pair.getLeft());
        }

        // try to get rid of the classes
        System.gc();
        System.gc();

        return reloadModules;
    }

    protected void unloadModule0(Module module)
    {
        // disable and cleanup the framework
        this.disableModule(module);
        this.loader.unloadModule(module);
        this.modules.remove(module.getId());
        this.moduleInfoMap.remove(module.getId());

        this.core.getLogFactory().shutdown(module);

        // null all the fields referencing this module
        for (Module m : this.modules.values())
        {
            Class moduleClass = m.getClass();
            for (Field field : moduleClass.getDeclaredFields())
            {
                if (field.getType() == module.getClass())
                {
                    try
                    {
                        field.setAccessible(true);
                        field.set(m, null);
                    }
                    catch (ReflectiveOperationException ignored)
                    {}
                }
            }
        }

        this.logger.debug("Unloaded module {}...", module.getId());
    }

    @Override
    public synchronized void reloadModule(Module module) throws ModuleException
    {
        this.reloadModule(module, false);
    }

    @Override
    public synchronized void reloadModule(Module module, boolean fromFile) throws ModuleException
    {
        if (fromFile)
        {
            List<ModuleInfo> reloadModules = this.unloadModule(module, true);
            this.enableModule(this.loadModule(module.getInfo().getPath()));
            this.loadModules(reloadModules);
        }
        else
        {
            if (module instanceof Reloadable)
            {
                ((Reloadable)module).reload();
            }
            else
            {
                this.logger.warn("The module '{}' is not natively reloadable, falling back to disabling and re-enabling.", module.getName());
                this.disableModule(module);
                this.enableModule(module);
            }
        }
    }

    public synchronized int reloadModules()
    {
        return this.reloadModules(false);
    }

    public synchronized int reloadModules(boolean fromFile)
    {
        int modules = 0;
        for (Module module : this.getModules())
        {
            try
            {
                this.reloadModule(module);
            }
            catch (ModuleException ex)
            {
                this.logger.error(ex, "Failed to reload ''{}''", module.getName());
            }
            ++modules;
        }
        return modules;
    }

    public synchronized void disableModules()
    {
        for (Module module : this.modules.values())
        {
            this.disableModule(module);
        }
    }

    public synchronized void unloadModules()
    {
        Set<Module> moduleSet = new THashSet<>(this.modules.values());
        for (Module module : moduleSet)
        {
            if (!this.modules.containsValue(module))
            {
                continue;
            }
            LinkedList<Pair<Module, Boolean>> unloadFirst = new LinkedList<>();
            this.resolveModulesForUnload(module, false, this.modules.values(), unloadFirst);
            for (Pair<Module, Boolean> pair : unloadFirst)
            {
                this.unloadModule0(pair.getLeft());
            }
            this.unloadModule0(module);
        }
        this.modules.clear();

        System.gc();
        System.gc();
    }

    @Override
    public synchronized void clean()
    {
        this.logger.debug("Unload modules...");
        Profiler.startProfiling("unload-modules");
        this.unloadModules();
        this.logger.debug("Unloading the modules took {} milliseconds!", Profiler.endProfiling("unload-modules", TimeUnit.MILLISECONDS));
        this.modules.clear();
        this.moduleInfoMap.clear();
        this.logger.debug("Shutting down the loader");
        this.loader.shutdown();
    }

    public CoreModule getCoreModule()
    {
        return this.coreModule;
    }
}
