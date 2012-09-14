package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.ModuleException;
import de.cubeisland.cubeengine.core.util.Validate;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleManager
{
    private final Map<String, Module> modules;
    private final Map<Class<? extends Module>, Module> classMap;
    private final Core core;
    private final ModuleLoader loader;
    private static final Logger logger = CubeEngine.getLogger();

    public ModuleManager(Core core)
    {
        this.modules = new ConcurrentHashMap<String, Module>();
        this.classMap = new THashMap<Class<? extends Module>, Module>();
        this.core = core;
        this.loader = new ModuleLoader(core);
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

    public synchronized void loadModules(File directory)
    {
        Validate.isDir(directory, "The given File is no directory!");

        Module module;
        ModuleInfo info;
        Map<String, ModuleInfo> moduleInfos = new HashMap<String, ModuleInfo>();
        logger.info("Loading modules...");
        for (File file : directory.listFiles((FileFilter)FileExtentionFilter.JAR))
        {
            try
            {
                info = this.loader.loadModuleInfo(file);
                module = this.getModule(info.getName());
                if (module != null)
                {
                    if (module.getInfo().getRevision() >= info.getRevision())
                    {
                        logger.warning(new StringBuilder("A newer or equal revision of the module '").append(info.getName()).append("' is already loaded!").toString());
                        continue;
                    }
                    else
                    {
                        this.disableModule(module);
                        this.modules.remove(module.getName());
                        logger.fine(new StringBuilder("A newer revision of '").append(info.getName()).append("' will replace the currently loaded version!").toString());
                    }
                }
                moduleInfos.put(info.getName().toLowerCase(Locale.ENGLISH), info);
            }
            catch (InvalidModuleException e)
            {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }

        for (String moduleName : moduleInfos.keySet())
        {
            try
            {
                this.loadModule(moduleName, moduleInfos);
            }
            catch (ModuleException e)
            {
                moduleInfos.remove(moduleName);
                logger.log(Level.SEVERE, new StringBuilder("Failed to load the module '").append(moduleName).append("'").toString(), e);
            }
        }
        logger.info("Finished loading modules!");
    }

    private boolean loadModule(String name, Map<String, ModuleInfo> moduleInfos) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException
    {
        return this.loadModule(name, moduleInfos, new Stack<String>(), false);
    }

    private boolean loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack, boolean soft) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException
    {
        name = name.toLowerCase(Locale.ENGLISH);
        if (this.modules.containsKey(name))
        {
            return true;
        }
        if (loadStack.contains(name))
        {
            throw new CircularDependencyException(loadStack.pop(), name);
        }
        ModuleInfo info = moduleInfos.get(name);
        if (info == null)
        {
            return soft;
        }
        loadStack.push(name);
        for (String dep : info.getSoftDependencies())
        {
            this.loadModule(dep, moduleInfos, loadStack, true);
        }
        for (String dep : info.getDependencies())
        {
            if (!this.loadModule(dep, moduleInfos, loadStack, false))
            {
                throw new MissingDependencyException(dep);
            }
        }
        Module module = this.loader.loadModule(info);
        loadStack.pop();
        if (!module.enable())
        {
            return false;
        }
        logger.log(Level.FINE, "Module {0}-r{1} successfully loaded!", new Object[] {info.getName(), info.getRevision()});
        this.modules.put(module.getName().toLowerCase(Locale.ENGLISH), module);
        return true;
    }

    public ModuleManager disableModule(Module module)
    {
        Validate.notNull(module, "The module must not be null!");
//        Set<String> dependingModules = module.getDependingModules();
//        for (String moduleName : dependingModules)
//        {
//            if (!name.equals(moduleName))
//            {
//                this.disableModule(name);
//            }
//        }
        module.disable();
        this.core.getEventManager().unregisterListener(module);
//        CommandManager.getInstance().unregisterAll(module);

        return this;
    }

    public ModuleManager disableModules()
    {
        for (Module module : this.modules.values())
        {
            this.disableModule(module);
        }
        return this;
    }

    public void clean()
    {
        this.disableModules();
        this.modules.clear();
    }
}