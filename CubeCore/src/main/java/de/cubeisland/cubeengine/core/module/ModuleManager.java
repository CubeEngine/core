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

        return this.modules.get(name);
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
                moduleInfos.put(info.getName(), info);
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
                this.loadModule(moduleName, moduleInfos, moduleStack);
            }
            catch (Exception e)
            {
                logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
    }

    private void loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> moduleStack) throws CircularDependencyException, MissingDependencyException, InvalidModuleException
    {
        this.loadModule(name, moduleInfos, moduleStack, false);
    }

    private void loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> moduleStack, boolean soft) throws CircularDependencyException, MissingDependencyException, InvalidModuleException
    {
        if (this.modules.containsKey(name))
        {
            return;
        }
        if (!moduleInfos.containsKey(name))
        {
            if (soft)
            {
                return;
            }
            throw new MissingDependencyException();
        }
        if (moduleStack.contains(name))
        {
            throw new CircularDependencyException();
        }
        moduleStack.push(name);
        ModuleInfo info = moduleInfos.get(name);
        for (String dep : info.getSoftDependencies())
        {
            loadModule(dep, moduleInfos, moduleStack, true);
        }
        for (String dep : info.getDependencies())
        {
            loadModule(dep, moduleInfos, moduleStack);
        }
        Module module = this.loader.loadModule(info);
        module.enable();
        this.modules.put(module.getName(), module);
        moduleStack.pop();
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
