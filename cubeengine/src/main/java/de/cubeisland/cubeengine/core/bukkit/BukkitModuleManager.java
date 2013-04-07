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
package de.cubeisland.cubeengine.core.bukkit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.cubeengine.core.module.BaseModuleManager;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;

public class BukkitModuleManager extends BaseModuleManager
{
    private final PluginManager pluginManager;

    public BukkitModuleManager(BukkitCore core, ClassLoader parentClassLoader)
    {
        super(core, parentClassLoader);
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
