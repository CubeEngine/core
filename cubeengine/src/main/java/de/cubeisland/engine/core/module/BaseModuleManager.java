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
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.annotation.Nullable;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.ModuleAlreadyLoadedException;
import de.cubeisland.engine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.engine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.engine.core.module.exception.CircularDependencyException;
import de.cubeisland.engine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.engine.core.module.exception.InvalidModuleException;
import de.cubeisland.engine.core.module.exception.MissingDependencyException;
import de.cubeisland.engine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.core.module.exception.MissingProviderException;
import de.cubeisland.engine.core.module.exception.ModuleException;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Version;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.JAR;


public abstract class BaseModuleManager implements ModuleManager
{
    private final Logger logger;
    protected final Core core;
    private final ModuleLoader loader;
    private final Map<String, Module> modules;
    private final Map<String, ModuleInfo> moduleInfos;
    private final Map<Class<? extends Module>, Module> classMap;
    private final CoreModule coreModule;

    private final Map<String, String> serviceProviders;

    public BaseModuleManager(Core core, ClassLoader parentClassLoader, ModuleLoggerFactory loggerFactory)
    {
        this.core = core;
        this.logger = core.getLog();
        this.loader = new ModuleLoader(core, parentClassLoader, loggerFactory);
        this.modules = new LinkedHashMap<>();
        this.moduleInfos = new THashMap<>();
        this.classMap = new THashMap<>();
        this.coreModule = new CoreModule();
                this.serviceProviders = new HashMap<>();
        this.coreModule.initialize(core, new ModuleInfo(core), core.getFileManager().getDataPath(), null, null, logger);
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

    public synchronized Module loadModule(Path modulePath) throws ModuleException
    {
        assert modulePath != null: "The file must not be null!";
        if (!Files.isRegularFile(modulePath))
        {
            throw new IllegalArgumentException("The given File is does not exist is not a normal file!");
        }

        ModuleInfo info = this.loader.loadModuleInfo(modulePath);
        if (info == null)
        {
            throw new InvalidModuleException("Failed to load the module info for file '" + modulePath.getFileName() + "'!");
        }

        if (this.moduleInfos.containsKey(info.getId()))
        {
            throw new ModuleAlreadyLoadedException(info.getName());
        }

        this.moduleInfos.put(info.getId(), info);
        return this.loadModule(info.getName(), this.moduleInfos);
    }

    public synchronized void loadModules(Path directory)
    {
        assert directory != null: "The directory must not be null!";
        assert Files.isDirectory(directory): "The given File is no directory!";

        Module module;
        ModuleInfo info;
        this.logger.info("Loading modules...");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory, JAR))
        {
            for (Path file : directoryStream)
            {
                try
                {
                    info = this.loader.loadModuleInfo(file);
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
                            this.unloadModule(module);
                            this.logger.info("A newer version of '" + info.getName() + "' will replace the currently loaded version!");
                        }
                    }
                    this.moduleInfos.put(info.getId(), info);
                }
                catch (InvalidModuleException e)
                {
                    this.logger.error("Failed to load the module from {}!", file);
                    this.logger.debug(e.getLocalizedMessage(), e);
                }
            }
        }
        catch (IOException e)
        {
            this.core.getLog().error("Failed to load modules!");
            this.core.getLog().debug(e.getLocalizedMessage(), e);
            return;
        }
        Collection<String> moduleNames = new HashSet<>(this.moduleInfos.keySet());
        for (String moduleName : moduleNames)
        {
            if (this.moduleInfos.get(moduleName).getServiceProviders() != null)
            {
                for (String service : this.moduleInfos.get(moduleName).getServiceProviders())
                {
                    this.serviceProviders.put(service, moduleName);
                }
            }
        }
        for (String moduleName : moduleNames)
        {
            try
            {
                this.loadModule(moduleName, this.moduleInfos);
            }
            catch (InvalidModuleException e)
            {
                this.moduleInfos.remove(moduleName);
                this.logger.debug("Failed to load the module '{}'", moduleName);
                this.logger.debug(e.getLocalizedMessage(), e);
            }
            catch (ModuleException e)
            {
                this.moduleInfos.remove(moduleName);
                this.logger.error("Failed to load the module '{}'", moduleName);
                this.logger.debug(e.getLocalizedMessage(), e);
            }
        }
        this.logger.info("Finished loading modules!");
    }

    private Module loadModule(String name, Map<String, ModuleInfo> moduleInfos) throws ModuleException
    {
        return this.loadModule(name, moduleInfos, new Stack<String>());
    }

    protected void validateModuleInfo(ModuleInfo info) throws MissingPluginDependencyException
    {}

    @SuppressWarnings("unchecked")
    @Nullable
    protected Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws ModuleException
    {
        name = name.toLowerCase(Locale.ENGLISH);
        Module module = null;
        if (this.modules.containsKey(name))
        {
            module = this.modules.get(name);
            if (module != null)
            {
                return module;
            }
            else
            {
                this.modules.remove(name);
            }
        }

        if (loadStack.contains(name))
        {
            throw new CircularDependencyException(loadStack.pop(), name);
        }
        ModuleInfo info = moduleInfos.get(name);
        if (info == null)
        {
            return null;
        }
        loadStack.push(name);

        this.validateModuleInfo(info);

        for (String loadAfterModule : info.getLoadAfter())
        {
            if (!loadStack.contains(loadAfterModule) && moduleInfos.containsKey(loadAfterModule))
            {
                this.loadModule(loadAfterModule, moduleInfos, loadStack);
            }
        }
        if (info.getServices() != null)
        {
            for (String service : info.getServices())
            {
                if (!this.core.getServiceManager().isServiceRegistered(service))
                {
                    String providerModule = this.serviceProviders.get(service);
                    if (providerModule == null)
                    {
                        throw new MissingProviderException(name, service);
                    }
                    this.loadModule(providerModule, moduleInfos);
                }
            }
        }
        Module depModule;
        String depName;
        for (Map.Entry<String, Version> dep : info.getSoftDependencies().entrySet())
        {
            depName = dep.getKey();
            depModule = this.loadModule(depName, moduleInfos, loadStack);
            if (dep.getValue().isNewerThan(Version.ZERO) && depModule.getInfo().getVersion().isOlderThan(dep.getValue()))
            {
                this.logger.warn("The module " + name + " requested a newer version of " + depName + "!");
            }
        }
        for (Map.Entry<String, Version> dep : info.getDependencies().entrySet())
        {
            depName = dep.getKey();
            depModule = this.loadModule(depName, moduleInfos, loadStack);
            if (depModule == null)
            {
                throw new MissingDependencyException(depName);
            }
            else
            {
                if (dep.getValue().isNewerThan(Version.ZERO) && depModule.getInfo().getVersion().isOlderThan(dep.getValue()))
                {
                    throw new IncompatibleDependencyException(name, depName, dep.getValue(), depModule.getInfo().getVersion());
                }
            }
        }

        // Load the modules logback.xml, if it exists
        try (JarFile jarFile = new JarFile(info.getPath().toFile()))
        {
            ZipEntry entry = jarFile.getEntry("logback.xml");
            if (entry != null)
            {
                JoranConfigurator logbackConfig = new JoranConfigurator();
                logbackConfig.setContext((LoggerContext)LoggerFactory.getILoggerFactory());
                try (InputStream is = jarFile.getInputStream(entry))
                {
                    logbackConfig.doConfigure(is);
                }
            }
        }
        catch (IOException ignored)
        {} // This should never happen
        catch (JoranException e)
        {
            module.getLog().warn("An error occurred while loading the modules logback.xml config");
            module.getLog().debug(e.getLocalizedMessage(), e);
        }

        module = this.loader.loadModule(info);
        loadStack.pop();

        Version requiredVersion;
        Module injectedModule;
        Class fieldType;
        Field[] fields = new Field[0];
        try
        {
            fields = module.getClass().getDeclaredFields();
        }
        catch (NoClassDefFoundError e)
        {
            module.getLog().warn("Failed to get the fields of the main class");
            module.getLog().debug(e.getLocalizedMessage(), e);
        }
        for (Field field : fields)
        {
            fieldType = field.getType();
            Inject injectAnnotation = field.getAnnotation(Inject.class);

            if (Module.class.isAssignableFrom(fieldType) && injectAnnotation != null)
            {
                injectedModule = this.classMap.get((Class<? extends Module>)fieldType);
                if (injectedModule == null || fieldType == module.getClass())
                {
                    if (injectAnnotation.require())
                    {
                        throw new MissingDependencyException(fieldType.getName());
                    }
                    continue;
                }
                requiredVersion = module.getInfo().getSoftDependencies().get(injectedModule.getId());
                if (requiredVersion != null && requiredVersion.isNewerThan(Version.ZERO) && injectedModule.getInfo().getVersion().isOlderThan(requiredVersion))
                {
                    if (injectAnnotation.require())
                    {
                        throw new MissingDependencyException(injectedModule.getName());
                    }
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
                catch (Exception e)
                {
                    if (injectAnnotation.require())
                    {
                        throw new MissingDependencyException(injectedModule.getName());
                    }
                    module.getLog().warn("Failed to inject a dependency: {}", injectedModule.getName());
                }
            }
        }

        this.modules.put(module.getId(), module);
        this.classMap.put(module.getClass(), module);

        return module;
    }

    public synchronized boolean enableModule(Module module)
    {
        module.getLog().info("Enabling version {}...", module.getVersion());
        Profiler.startProfiling("enable-module");
        boolean result = module.enable();
        final long enableTime = Profiler.endProfiling("enable-module", TimeUnit.MICROSECONDS);
        if (!result)
        {
            module.getLog().error("Module failed to load.");
        }
        else
        {
            this.core.getEventManager().fireEvent(new ModuleEnabledEvent(this.core, module));
            module.getLog().info("Successfully enabled within {} microseconds!", enableTime);
        }
        return result;
    }

    public synchronized void enableModules()
    {
        for (Module module : this.modules.values())
        {
            this.enableModule(module);
        }
    }

    public synchronized void disableModule(Module module)
    {
        Profiler.startProfiling("disable-module");
        try
        {
            module.disable();
            this.core.getUserManager().cleanup(module);
            this.core.getEventManager().removeListeners(module);
            this.core.getPermissionManager().removePermissions(module);
            this.core.getTaskManager().cancelTasks(module);
            this.core.getCommandManager().removeCommands(module);
            this.core.getApiServer().unregisterApiHandlers(module);

            this.core.getEventManager().fireEvent(new ModuleDisabledEvent(this.core, module));
        }
        finally
        {
            module.getLog().info("Module disabled within {} microseconds", Profiler.endProfiling("disable-module", TimeUnit.MICROSECONDS));
        }
    }

    public synchronized void unloadModule(final Module module)
    {
        if (this.modules.remove(module.getId()) == null)
        {
            return;
        }

        Map.Entry<String, Module> entry;
        Iterator<Map.Entry<String, Module>> it = this.modules.entrySet().iterator();
        while (it.hasNext())
        {
            entry = it.next();
            if (this.modules.containsKey(entry.getKey()))
            {
                Module m = entry.getValue();
                if (m.getId().equals(module.getId()))
                {
                    continue;
                }
                if (m.getInfo().getDependencies().containsKey(entry.getKey()) || m.getInfo().getSoftDependencies().containsKey(entry.getKey()))
                {
                    this.unloadModule(m);
                }
            }
        }

        Profiler.startProfiling("unload-" + module.getId());
        this.disableModule(module);
        this.loader.unloadModule(module);
        this.moduleInfos.remove(module.getId());
        ((ch.qos.logback.classic.Logger)module.getLog()).detachAndStopAllAppenders();

        this.logger.debug(Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS) + "ms - null fields");
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
                    catch (Exception ignored)
                    {}
                }
            }
        }
        this.logger.debug(Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS)+ "ms - classloader");
        ClassLoader classLoader = module.getClassLoader();
        if (classLoader instanceof ModuleClassLoader)
        {
            ((ModuleClassLoader)classLoader).shutdown();
        }
        else if (classLoader instanceof URLClassLoader)
        {
            try
            {
                ((URLClassLoader)classLoader).close();
            }
            catch (IOException e)
            {
                module.getLog().warn("Failed to close the class loader of {}!", module.getName());
                module.getLog().debug(e.getLocalizedMessage(), e);
            }
        }
        else
        {
            module.getLog().debug("Class loader cannot be closed.");
        }
        this.logger.debug(Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS)+ "ms - Before GC ");
        System.gc();
        System.gc();
        this.logger.debug("Unloading '" + module.getName() + "' took {} milliseconds!", Profiler
            .endProfiling("unload-" + module.getId(), TimeUnit.MILLISECONDS));

        assert !this.modules.containsKey(module.getId()): "Module not properly removed (modules)!";
        assert !this.moduleInfos.containsKey(module.getId()): "Module not properly removed (moduleInfos)!";
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
            this.unloadModule(module);
            this.enableModule(this.loadModule(module.getInfo().getPath()));
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
            catch (ModuleException e)
            {
                this.logger.error("Failed to reload ''{}''", module.getName());
                this.logger.debug(e.getLocalizedMessage(), e);
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
        for (Module module : new THashSet<>(this.modules.values()))
        {
            this.unloadModule(module);
        }
        this.modules.clear();
    }

    @Override
    public synchronized void clean()
    {
        this.logger.debug("Unload modules...");
        Profiler.startProfiling("unload-modules");
        this.unloadModules();
        this.logger.debug("Unloading the modules took {} milliseconds!", Profiler.endProfiling("unload-modules", TimeUnit.MILLISECONDS));
        this.modules.clear();
        this.moduleInfos.clear();
        this.logger.debug("Shutting down the loader");
        this.loader.shutdown();
    }

    public CoreModule getCoreModule()
    {
        return this.coreModule;
    }
}
