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
package de.cubeisland.cubeengine.core.module;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.module.event.ModuleDisabledEvent;
import de.cubeisland.cubeengine.core.module.event.ModuleEnabledEvent;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.cubeengine.core.module.exception.ModuleException;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.Version;

import gnu.trove.map.hash.THashMap;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;

public abstract class BaseModuleManager implements ModuleManager
{
    private final Logger logger;
    protected final Core core;
    private final ModuleLoader loader;
    private final Map<String, Module> modules;
    private final Map<String, ModuleInfo> moduleInfos;
    private final Map<Class<? extends Module>, Module> classMap;
    private final CoreModule coreModule;

    public BaseModuleManager(Core core, ClassLoader parentClassLoader)
    {
        this.core = core;
        this.logger = core.getLog();
        this.loader = new ModuleLoader(core, parentClassLoader);
        this.modules = new THashMap<String, Module>();
        this.moduleInfos = new THashMap<String, ModuleInfo>();
        this.classMap = new THashMap<Class<? extends Module>, Module>();
        this.coreModule = new CoreModule();
        this.coreModule.initialize(core, new ModuleInfo(core), core.getFileManager().getDataFolder(), core.getLog(), null, null);
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
        return new ArrayList<Module>(this.modules.values());
    }

    public synchronized Module loadModule(File moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        assert moduleFile != null: "The file must not be null!";
        if (!moduleFile.isFile())
        {
            throw new IllegalArgumentException("The given File is does not exist is not a normal file!");
        }

        ModuleInfo info = this.loader.loadModuleInfo(moduleFile);
        if (info == null)
        {
            throw new InvalidModuleException("Failed to load the module info for file '" + moduleFile.getName() + "'!");
        }

        ModuleInfo oldInfo = this.moduleInfos.put(info.getId(), info);
        if (oldInfo != null)
        {
            Module oldModule = this.modules.get(oldInfo.getId());
            if (oldModule != null)
            {
                this.unloadModule(oldModule);
            }
        }

        Module module = this.loadModule(info.getName(), this.moduleInfos);

        return module;
    }

    public synchronized void loadModules(File directory)
    {
        assert directory != null: "The directory must not be null!";
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("The given File is no directory!");
        }

        Module module;
        ModuleInfo info;
        this.logger.log(NOTICE, "Loading modules...");
        for (File file : directory.listFiles((FileFilter)FileExtentionFilter.JAR))
        {
            try
            {
                info = this.loader.loadModuleInfo(file);
                module = this.getModule(info.getId());
                if (module != null)
                {
                    if (module.getInfo().getVersion().compareTo(info.getVersion()) >= 0)
                    {
                        this.logger.log(WARNING, "A newer or equal version of the module '" + info.getName() + "' is already loaded!");
                        continue;
                    }
                    else
                    {
                        this.unloadModule(module);
                        this.logger.log(NOTICE, "A newer version of '" + info.getName() + "' will replace the currently loaded version!");
                    }
                }
                this.moduleInfos.put(info.getId(), info);
            }
            catch (InvalidModuleException e)
            {
                this.logger.log(ERROR, e.getLocalizedMessage(), e);
            }
        }

        for (String moduleName : this.moduleInfos.keySet())
        {
            try
            {
                this.loadModule(moduleName, this.moduleInfos);
            }
            catch (InvalidModuleException e)
            {
                this.moduleInfos.remove(moduleName);
                this.logger.log(DEBUG, "Failed to load the module '" + moduleName + "'", e);
            }
            catch (ModuleException e)
            {
                this.moduleInfos.remove(moduleName);
                this.logger.log(ERROR, "Failed to load the module '" + moduleName + "'", e);
            }
        }
        this.logger.log(NOTICE, "Finished loading modules!");
    }

    private Module loadModule(String name, Map<String, ModuleInfo> moduleInfos) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        return this.loadModule(name, moduleInfos, new Stack<String>());
    }

    protected abstract void validatePluginDependencies(Set<String> plugins) throws MissingPluginDependencyException;

    protected abstract Map<Class, Object> getPluginClassMap();

    @SuppressWarnings("unchecked")
    protected Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        name = name.toLowerCase(Locale.ENGLISH);
        Module module = this.modules.get(name);
        if (module != null)
        {
            return module;
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

        this.validatePluginDependencies(info.getPluginDependencies());

        for (String loadAfterModule : info.getLoadAfter())
        {
            if (!loadStack.contains(loadAfterModule))
            {
                this.loadModule(loadAfterModule, moduleInfos, loadStack);
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
                this.logger.log(WARNING, "The module " + name + " requested a newer version of " + depName + "!");
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
        module = this.loader.loadModule(info);
        loadStack.pop();

        Map<Class, Object> pluginClassMap = this.getPluginClassMap();
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
            module.getLog().log(WARNING, "Failed to get the fields of the main class: " + e.getLocalizedMessage(), e);
        }
        for (Field field : fields)
        {
            fieldType = field.getType();
            if (Module.class.isAssignableFrom(fieldType))
            {
                injectedModule = this.classMap.get((Class<? extends Module>)fieldType);
                if (injectedModule == null)
                {
                    continue;
                }
                if (fieldType == module.getClass())
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
                catch (Exception e)
                {
                    module.getLog().log(WARNING, "Failed to inject a dependency: {0}", injectedModule.getName());
                }
            }
            else
            {
                if (Plugin.class.isAssignableFrom(fieldType))
                {
                    Object plugin = pluginClassMap.get(fieldType);
                    if (plugin == null)
                    {
                        continue;
                    }
                    field.setAccessible(true);
                    try
                    {
                        field.set(module, plugin);
                    }
                    catch (Exception e)
                    {
                        module.getLog().log(WARNING, "Failed to inject a plugin dependency: {0}", String.valueOf(plugin));
                    }
                }
            }
        }

        this.modules.put(module.getId(), module);
        this.classMap.put(module.getClass(), module);

        return module;
    }

    public synchronized boolean enableModule(Module module)
    {
        module.getLog().log(INFO, "Enabling version {0}...", module.getVersion());
        Profiler.startProfiling("enable-module");
        boolean result = module.enable();
        final long enableTime = Profiler.endProfiling("enable-module", TimeUnit.MICROSECONDS);
        if (!result)
        {
            module.getLog().log(ERROR, " Module failed to load.");
        }
        else
        {
            this.core.getEventManager().fireEvent(new ModuleEnabledEvent(this.core, module));
            module.getLog().log(INFO, "Successfully enabled within {0} microseconds!", enableTime);
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
        module.disable();
        this.core.getUserManager().cleanup(module);
        this.core.getEventManager().removeListeners(module);
        this.core.getPermissionManager().removePermissions(module);
        this.core.getTaskManager().cancelTasks(module);
        this.core.getCommandManager().removeCommands(module);
        this.core.getApiServer().unregisterApiHandlers(module);

        this.core.getEventManager().fireEvent(new ModuleDisabledEvent(this.core, module));
        module.getLog().log(INFO, "Module disabled within {0} microseconds", Profiler.endProfiling("disable-module", TimeUnit.MICROSECONDS));
    }

    public synchronized void unloadModule(Module module)
    {
        if (this.modules.remove(module.getId()) == null)
        {
            return;
        }

        Set<Module> disable = new HashSet<Module>();
        for (Module m : this.modules.values())
        {
            if (m.getInfo().getDependencies().containsKey(module.getId()) || m.getInfo().getSoftDependencies().containsKey(module.getId()))
            {
                disable.add(m);
            }
        }

        for (Module m : disable)
        {
            this.unloadModule(m);
        }
        Profiler.startProfiling("unload-" + module.getId());
        this.disableModule(module);
        this.loader.unloadModule(module);
        this.moduleInfos.remove(module.getId());

        this.logger.log(DEBUG, Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS)+ "ms - null fields");
        // null all the fields referencing this module
        for (Module m : this.modules.values())
        {
            Class moduleClass = m.getClass();
            for (Field field : moduleClass.getDeclaredFields())
            {
                if (field.getType() == module.getClass())
                    try
                    {
                        field.setAccessible(true);
                        field.set(m, null);
                    }
                    catch (Exception ignored)
                    {}
            }
        }
        this.logger.log(DEBUG, Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS)+ "ms - classloader");
        ClassLoader classLoader = module.getClassLoader();
        if (classLoader instanceof ModuleClassLoader)
        {
            ((ModuleClassLoader)classLoader).shutdown();
        }
        this.logger.log(DEBUG, Profiler.getCurrentDelta("unload-" + module.getId(), TimeUnit.MILLISECONDS)+ "ms - Before GC ");
        System.gc();
        System.gc();
        this.logger.log(DEBUG, "Unloading '" + module.getName() + "' took {0} milliseconds!", Profiler
            .endProfiling("unload-" + module.getId(), TimeUnit.MILLISECONDS));
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
            this.loadModule(module.getInfo().getFile());
        }
        else
        {
            if (module instanceof Reloadable)
            {
                ((Reloadable)module).reload();
            }
            else
            {
                this.logger.log(NOTICE, "The module ''{0}'' is not natively reloadable, falling back to disabling and re-enabling.", module.getName());
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
                this.logger.log(ERROR, "Failed to reload ''{0}''", module.getName());
                this.logger.log(ERROR, e.getLocalizedMessage(), e);
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
        Iterator<Map.Entry<String, Module>> it = this.modules.entrySet().iterator();
        while (it.hasNext())
        {
            this.unloadModule(it.next().getValue());
            it.remove();
        }
        this.modules.clear();
    }

    @Override
    public synchronized void clean()
    {
        this.logger.log(DEBUG, "Unload modules...");
        Profiler.startProfiling("unload-modules");
        this.unloadModules();
        this.logger.log(DEBUG, "Unloading the modules took {0} milliseconds!", Profiler.endProfiling("unload-modules", TimeUnit.MILLISECONDS));
        this.modules.clear();
        this.moduleInfos.clear();
        this.logger.log(DEBUG, "Shutting down the loader");
        this.loader.shutdown();
    }

    public CoreModule getCoreModule()
    {
        return this.coreModule;
    }
}
