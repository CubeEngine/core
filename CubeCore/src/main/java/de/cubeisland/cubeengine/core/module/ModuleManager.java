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
    private final Logger logger;

    public ModuleManager(CubeCore core)
    {
        this.modules = new THashMap<String, Module>();
        this.core = core;
        this.pm = core.getServer().getPluginManager();
        this.loader = new ModuleLoader(core);
        this.logger = core.getCoreLogger();
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
                        this.logger.warning(new StringBuilder("A newer or equal revision of the module '").append(info.getName()).append("' is already loaded!").toString());
                        continue;
                    }
                    else
                    {
                        this.disableModule(module);
                        this.modules.remove(module.getName());
                        this.logger.fine(new StringBuilder("A newer revision of '").append(info.getName()).append("' will replace the currently loaded version!").toString());
                    }
                }
                moduleInfos.put(info.getName().toLowerCase(), info);
            }
            catch (InvalidModuleException e)
            {
                this.logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }

        for (String moduleName : moduleInfos.keySet())
        {
            try
            {
                this.loadModule(moduleName, moduleInfos);
            }
            catch (Exception e)
            {
                moduleInfos.remove(moduleName);
                logger.log(Level.SEVERE, new StringBuilder("Failed to load the module '").append(moduleName).append("'").toString(), e);
            }
        }
        logger.info("Finished loading modules!");
    }

    private boolean loadModule(String name, Map<String, ModuleInfo> moduleInfos) throws CircularDependencyException, MissingDependencyException, InvalidModuleException
    {
        return this.loadModule(name, moduleInfos, new Stack<String>(), false);
    }

    private boolean loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack, boolean soft) throws CircularDependencyException, MissingDependencyException, InvalidModuleException
    {
        name = name.toLowerCase();
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
            loadModule(dep, moduleInfos, loadStack, true);
        }
        for (String dep : info.getDependencies())
        {
            if (!loadModule(dep, moduleInfos, loadStack, false))
            {
                throw new MissingDependencyException(dep);
            }
        }
        Module module = this.loader.loadModule(info);
        loadStack.pop();
        try
        {
            module.enable();
        }
        catch (Throwable t)
        {
            this.logger.log(Level.SEVERE, "An error occurred while enabling the module", t);
            return false;
        }
        this.core.getCoreLogger().info(new StringBuilder("Module '").append(info.getName()).append("' r").append(info.getRevision()).append(" successfully loaded!").toString());
        this.modules.put(module.getName().toLowerCase(), module);
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
        HandlerList.unregisterAll((BukkitPluginWrapper)module.getPluginWrapper()); // TODO abstract this
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
