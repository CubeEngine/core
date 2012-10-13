package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.module.exception.*;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleManager
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

    public Collection<Module> getModules()
    {
        return this.modules.values();
    }

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

        return this.loadModule(info.getName(), this.moduleInfos);
    }

    public synchronized void loadModules(File directory)
    {
        Validate.notNull(directory, "The directoy must not be null!");
        if (!directory.isDirectory())
        {
            throw new IllegalArgumentException("The given File is no directory!");
        }

        Module module;
        ModuleInfo info;
        LOGGER.info("Loading modules...");
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
                        LOGGER.warning(new StringBuilder("A newer or equal revision of the module '").append(info.getName()).append("' is already loaded!").toString());
                        continue;
                    }
                    else
                    {
                        this.unloadModule(module);
                        LOGGER.fine(new StringBuilder("A newer revision of '").append(info.getName()).append("' will replace the currently loaded version!").toString());
                    }
                }
                this.moduleInfos.put(info.getId(), info);
            }
            catch (InvalidModuleException e)
            {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
                LOGGER.log(Level.SEVERE, new StringBuilder("Failed to load the module '").append(moduleName).append("'").toString(), e);
            }
        }
        LOGGER.info("Finished loading modules!");
        
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
                LOGGER.log(Level.WARNING, "The module {0} requested a newer revision of {1}!", new Object[]
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
                    LOGGER.log(Level.WARNING, "Failed to inject a dependency into {0}: {1}", new Object[]
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
                        LOGGER.log(Level.WARNING, "Failed to inject a plugin dependency into {0}: {1}", new Object[]
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

        LOGGER.log(Level.FINE, "Module {0}-r{1} successfully loaded!", new Object[]
            {
                info.getName(), info.getRevision()
            });
        this.modules.put(module.getId(), module);

        return module;
    }
    
    public boolean enableModule(Module module)
    {
        return this.enableModule(module, true);
    }
    
    private boolean enableModule(Module module, boolean reloadHelp)
    {
        boolean result = module.enable();
        if (reloadHelp)
        {
            BukkitUtils.reloadHelpMap();
        }
        return result;
    }
    
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

    public void disableModule(Module module)
    {
        this.disableModule(module, true);
    }

    public void disableModule(Module module, boolean reloadHelp)
    {
        Validate.notNull(module, "The module must not be null!");
        module.disable();
        this.core.getEventManager().unregisterListener(module);
        this.core.getPermissionManager().unregisterPermissions(module);
        this.core.getTaskManager().cancelTasks(module);
        this.core.getCommandManager().unregister(module);
    }

    public void unloadModule(Module module)
    {
//        Set<String> dependingModules = module.getDependingModules();
//        for (String moduleName : dependingModules)
//        {
//            if (!name.equals(moduleName))
//            {
//                this.disableModule(name);
//            }
//        }
        this.disableModule(module);
        this.loader.unloadModule(module);
        this.modules.remove(module.getName());
    }

    public void disableModules()
    {
        for (Module module : this.modules.values())
        {
            this.disableModule(module, false);
        }
        BukkitUtils.reloadHelpMap();
    }

    public void clean()
    {
        this.disableModules();
        this.modules.clear();
    }
}