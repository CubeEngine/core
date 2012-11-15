package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.cubeengine.core.module.exception.ModuleException;
import de.cubeisland.cubeengine.core.util.Cleanable;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * This class manages the modules.
 */
public class ModuleManager implements Cleanable
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    private final Core core;
    private final ModuleLoader loader;
    private final Map<String, Module> modules;
    private final Map<String, ModuleInfo> moduleInfos;
    private final Map<Class<? extends Module>, Module> classMap;
    private final PluginManager pluginManager;

    public ModuleManager(Core core)
    {
        this.core = core;
        this.loader = new ModuleLoader(core);
        this.modules = new ConcurrentHashMap<String, Module>();
        this.moduleInfos = new ConcurrentHashMap<String, ModuleInfo>();
        this.classMap = new THashMap<Class<? extends Module>, Module>();

        this.pluginManager = ((BukkitCore)core).getServer().getPluginManager();
    }

    public Module getModule(String name)
    {
        if (name == null)
        {
            return null;
        }

        return this.modules.get(name.toLowerCase(Locale.ENGLISH));
    }

    /**
     * This method returns a collection of the modules
     *
     * @return the modules
     */
    public Collection<Module> getModules()
    {
        return this.modules.values();
    }

    /**
     * Loads a module
     *
     * @param moduleFile the file to load the module from
     * @return the loaded module
     *
     * @throws InvalidModuleException           if the file is not a valid module
     * @throws CircularDependencyException      if the module defines a circular dependencies
     * @throws MissingDependencyException       if the module has a missing dependency
     * @throws IncompatibleDependencyException  if the module needs a newer dependency
     * @throws IncompatibleCoreException        if the module depends on a newer core
     * @throws MissingPluginDependencyException if the module depends on a missing plugin
     */
    public synchronized Module loadModule(File moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        Validate.notNull(moduleFile, "The file must not be null!");
        if (!moduleFile.isFile())
        {
            throw new IllegalArgumentException("The given File is does not exist is not a normal file!");
        }

        ModuleInfo info = this.loader.loadModuleInfo(moduleFile);

        info = this.moduleInfos.put(info.getId(), info);
        if (info != null)
        {
            Module oldModule = this.modules.get(info.getId());
            if (oldModule != null)
            {
                this.unloadModule(oldModule);
            }
        }

        Module module = this.loadModule(info.getName(), this.moduleInfos);
        BukkitUtils.reloadHelpMap();

        return module;
    }

    /**
     * This method loads all modules from a directory
     *
     * @param directory the directory to load from
     */
    public synchronized void loadModules(File directory)
    {
        Validate.notNull(directory, "The directoy must not be null!");
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("The given File is no directory!");
        }

        Module module;
        ModuleInfo info;
        LOGGER.log(LogLevel.NOTICE, "Loading modules...");
        for (File file : directory.listFiles((FileFilter)FileExtentionFilter.JAR))
        {
            try
            {
                info = this.loader.loadModuleInfo(file);
                module = this.getModule(info.getId());
                if (module != null)
                {
                    if (module.getInfo().getRevision() >= info.getRevision())
                    {
                        LOGGER.log(LogLevel.WARNING, new StringBuilder("A newer or equal revision of the module '").append(info.getName()).append("' is already loaded!").toString());
                        continue;
                    }
                    else
                    {
                        this.unloadModule(module);
                        LOGGER.log(LogLevel.NOTICE, new StringBuilder("A newer revision of '").append(info.getName()).append("' will replace the currently loaded version!").toString());
                    }
                }
                this.moduleInfos.put(info.getId(), info);
            }
            catch (InvalidModuleException e)
            {
                LOGGER.log(LogLevel.ERROR, e.getLocalizedMessage(), e);
            }
        }

        for (String moduleName : this.moduleInfos.keySet())
        {
            try
            {
                this.loadModule(moduleName, this.moduleInfos);
            }
            catch (ModuleException e)
            {
                this.moduleInfos.remove(moduleName);
                LOGGER.log(LogLevel.ERROR, new StringBuilder("Failed to load the module '").append(moduleName).append("'").toString(), e);
            }
        }
        LOGGER.log(LogLevel.NOTICE, "Finished loading modules!");

        BukkitUtils.reloadHelpMap();
    }

    private Module loadModule(String name, Map<String, ModuleInfo> moduleInfos) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        return this.loadModule(name, moduleInfos, new Stack<String>());
    }

    private Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
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

        for (String dep : info.getPluginDependencies())
        {
            if (this.pluginManager.getPlugin(dep) == null)
            {
                throw new MissingPluginDependencyException(dep);
            }
        }

        Module depModule;
        String depName;
        for (Map.Entry<String, Integer> dep : info.getSoftDependencies().entrySet())
        {
            depName = dep.getKey();
            depModule = this.loadModule(depName, moduleInfos, loadStack);
            if (dep.getValue() > -1 && depModule.getInfo().getRevision() < dep.getValue())
            {
                LOGGER.log(LogLevel.WARNING, "The module {0} requested a newer revision of {1}!", new Object[]
                    {
                        name, depName
                    });
            }
        }
        for (Map.Entry<String, Integer> dep : info.getDependencies().entrySet())
        {
            depName = dep.getKey();
            depModule = this.loadModule(depName, moduleInfos, loadStack);
            if (depModule == null)
            {
                throw new MissingDependencyException(depName);
            }
            else
            {
                if (dep.getValue() > -1 && depModule.getInfo().getRevision() < dep.getValue())
                {
                    throw new IncompatibleDependencyException(name, depName, dep.getValue(), depModule.getInfo().getRevision());
                }
            }
        }
        module = this.loader.loadModule(info);
        loadStack.pop();

        Plugin[] plugins = this.pluginManager.getPlugins();
        Map<Class, Plugin> pluginClassMap = new HashMap<Class, Plugin>(plugins.length);
        for (Plugin plugin : plugins)
        {
            pluginClassMap.put(plugin.getClass(), plugin);
        }

        Integer requiredVersion;
        Module injectedModule;
        Class fieldType;
        for (Field field : module.getClass().getDeclaredFields())
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
                if (requiredVersion != null && requiredVersion > -1 && injectedModule.getInfo().getRevision() < requiredVersion)
                {
                    continue;
                }
                field.setAccessible(true);
                try
                {
                    field.set(module, injectedModule);
                }
                catch (Exception e)
                {
                    LOGGER.log(LogLevel.WARNING, "Failed to inject a dependency into {0}: {1}", new Object[]
                        {
                            name, injectedModule.getName()
                        });
                }
            }
            else
            {
                if (Plugin.class.isAssignableFrom(fieldType))
                {
                    Plugin plugin = pluginClassMap.get(fieldType);
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
                        LOGGER.log(LogLevel.WARNING, "Failed to inject a plugin dependency into {0}: {1}", new Object[]
                            {
                                name, plugin.getName()
                            });
                    }
                }
            }
        }


        if (!module.enable())
        {
            return null;
        }

        this.classMap.put(module.getClass(), module);

        LOGGER.log(LogLevel.INFO, "Module {0}-r{1} successfully loaded!", new Object[]
            {
                info.getName(), info.getRevision()
            });
        this.modules.put(module.getId(), module);

        return module;
    }

    /**
     * Enables a module
     *
     * @param module the module
     * @return true if it succeeded
     */
    public boolean enableModule(Module module)
    {
        return this.enableModule(module, true);
    }

    /**
     * Enables a module and reloads the Bukkit help map if wanted
     *
     * @param module     the module
     * @param reloadHelp whether to reload the help map
     * @return true if it succeeded
     */
    private boolean enableModule(Module module, boolean reloadHelp)
    {
        boolean result = module.enable();
        if (reloadHelp)
        {
            BukkitUtils.reloadHelpMap();
        }
        return result;
    }

    /**
     * This method enables all modules that provide world generators
     */
    public void enableWorldGeneratorModules()
    {
        for (Module module : this.modules.values())
        {
            if (module.getInfo().providesWorldGenerator())
            {
                module.enable();
            }
        }
    }

    /**
     * This method enables all modules or at least all that don't provide world generators
     *
     * @param worldGenerators whether to also load the world generator-providing modules
     */
    public void enableModules(boolean worldGenerators)
    {
        for (Module module : this.modules.values())
        {
            if (!module.getInfo().providesWorldGenerator() || worldGenerators)
            {
                module.enable();
            }
        }

        BukkitUtils.reloadHelpMap();
    }

    /**
     * This method disables a module
     *
     * @param module the module
     */
    public void disableModule(Module module)
    {
        this.disableModule(module, true);
    }

    /**
     * This method disables a module and reloads the Bukkit's help map
     *
     * @param module     the module
     * @param reloadHelp whether to reload help map
     */
    private void disableModule(Module module, boolean reloadHelp)
    {
        Validate.notNull(module, "The module must not be null!");

        module.disable();
        this.core.getEventManager().unregisterListener(module);
        this.core.getPermissionManager().unregisterPermissions(module);
        this.core.getTaskManager().cancelTasks(module);
        this.core.getCommandManager().unregister(module);
        this.core.getApiServer().unregisterApiHandlers(module);
        
        if (reloadHelp)
        {
            BukkitUtils.reloadHelpMap();
        }
        this.core.getUserManager().clearAttributes(module); // Clean up saved attributes
    }

    /**
     * This method tries to unload a module be remoing as many references as possible.
     * this means:
     * - disable all modules that depend in the given module
     * - disable the module
     * - remove its classloader and all the reference to it
     * - remove the module from the module map
     *
     * @param module the module to unload
     */
    public void unloadModule(Module module)
    {
        if (!this.modules.containsValue(module))
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

        this.disableModule(module);
        this.loader.unloadModule(module);
        this.modules.remove(module.getName());
    }

    /**
     * This method disables all modules
     */
    public void disableModules()
    {
        for (Module module : this.modules.values())
        {
            this.disableModule(module, false);
        }
        BukkitUtils.reloadHelpMap();
    }

    @Override
    public void clean()
    {
        this.disableModules();
        this.modules.clear();
    }
}