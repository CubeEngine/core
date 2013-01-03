package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.module.BaseModuleManager;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.exception.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BukkitModuleManager extends BaseModuleManager
{
    private final PluginManager pluginManager;

    public BukkitModuleManager(BukkitCore core)
    {
        super(core);
        this.pluginManager = core.getServer().getPluginManager();
    }

    @Override
    public synchronized Module loadModule(File moduleFile) throws InvalidModuleException, CircularDependencyException, MissingDependencyException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        Module module = super.loadModule(moduleFile);
        BukkitUtils.reloadHelpMap();
        return module;
    }

    @Override
    public synchronized void loadModules(File directory)
    {
        super.loadModules(directory);
        BukkitUtils.reloadHelpMap();
    }

    @Override
    public void enableModules(boolean worldGenerators)
    {
        super.enableModules(worldGenerators);
        BukkitUtils.reloadHelpMap();
    }

    @Override
    protected void validatePluginDependencies(Set<String> plugins) throws MissingPluginDependencyException
    {
        for (String plugin : plugins)
        {
            if (this.pluginManager.getPlugin(plugin) == null)
            {
                throw new MissingPluginDependencyException(plugin);
            }
        }
    }

    @Override
    protected Map<Class, Object> getPluginClassMap()
    {
        Plugin[] plugins = this.pluginManager.getPlugins();
        Map<Class, Object> pluginClassMap = new HashMap<Class, Object>(plugins.length);
        for (Plugin plugin : plugins)
        {
            pluginClassMap.put(plugin.getClass(), plugin);
        }
        return pluginClassMap;
    }

    @Override
    public void disableModule(Module module)
    {
        super.disableModule(module);
        BukkitUtils.reloadHelpMap();
    }

    @Override
    public boolean enableModule(Module module)
    {
        boolean result = super.enableModule(module);
        BukkitUtils.reloadHelpMap();
        return result;
    }
}
