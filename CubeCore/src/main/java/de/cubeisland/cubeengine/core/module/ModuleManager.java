package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.filesystem.FileExtentionFilter;
import de.cubeisland.cubeengine.core.util.Validate;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleManager
{
    private final Map<String, Module> modules;
    private final CubeCore core;
    private final PluginManager pm;
    private final ModuleLoader loader;

    public ModuleManager(CubeCore core)
    {
        this.modules = new THashMap<String, Module>();
        this.core = core;
        this.pm = core.getServer().getPluginManager();
        this.loader = new ModuleLoader(core);
    }

    public Module getModule(String name)
    {
        if (name == null)
        {
            return null;
        }

        return this.modules.get(name.toLowerCase());
    }

    public Collection<Module> getModules()
    {
        return this.modules.values();
    }

    public void loadModules(File directory)
    {
        Validate.isDir(directory, "The give dir is no dir!");
        Logger logger = this.core.getLogger();

        Module module;
        ModuleInfo info;
        HashMap<String, ModuleInfo> moduleInfos = new HashMap<String, ModuleInfo>();
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
                        logger.warning(new StringBuilder("A with a newer or equal revision of the module '").append(info.getName()).append("' is already loaded!").toString());
                        continue;
                    }
                    else
                    {
                        logger.fine(new StringBuilder("A newer revision of '").append(info.getName()).append("' will replace the currently loaded version!").toString());
                    }
                }
                moduleInfos.put(info.getName().toLowerCase(), info);
            }
            catch (InvalidModuleException e)
            {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }

        Stack<String> moduleStack = new Stack<String>();
        for (String moduleName : moduleInfos.keySet())
        {
            try
            {
                this.loadModule(moduleName, moduleInfos, moduleStack, false);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, new StringBuilder("Failed to load the module '").append(moduleName).append("'").toString(), e);
            }
        }
        logger.info("Finished loading modules!");
    }

    private boolean loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> moduleStack, boolean soft) throws CircularDependencyException, MissingDependencyException, InvalidModuleException
    {
        name = name.toLowerCase();
        if (this.modules.containsKey(name))
        {
            return true;
        }
        if (moduleStack.contains(name))
        {
            throw new CircularDependencyException();
        }
        ModuleInfo info = moduleInfos.get(name);
        if (info == null)
        {
            return soft;
        }
        moduleStack.push(name);
        for (String dep : info.getSoftDependencies())
        {
            loadModule(dep, moduleInfos, moduleStack, true);
        }
        for (String dep : info.getDependencies())
        {
            if (!loadModule(dep, moduleInfos, moduleStack, false))
            {
                throw new MissingDependencyException(dep);
            }
        }
        Module module = this.loader.loadModule(info);
        module.enable();
        this.core.getCoreLogger().info(new StringBuilder("Module '").append(info.getName()).append("' Revision ").append(info.getRevision()).append(" successfully loaded!").toString());
        this.modules.put(module.getName().toLowerCase(), module);
        moduleStack.pop();
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
        HandlerList.unregisterAll(module.getPluginWrapper());
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
