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
package de.cubeisland.engine.core.bukkit;

import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.module.BaseModuleManager;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleInfo;
import de.cubeisland.engine.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.core.module.exception.ModuleDependencyException;

public class BukkitModuleManager extends BaseModuleManager
{
    private final BukkitCore core;
    private final PluginManager pluginManager;

    public BukkitModuleManager(BukkitCore core, ClassLoader parentClassLoader)
    {
        super(core, new BukkitServiceManager(core), new BukkitModuleLoader(core, parentClassLoader));
        this.pluginManager = core.getServer().getPluginManager();
        this.core = core;
    }

    @Override
    public BukkitServiceManager getServiceManager()
    {
        return (BukkitServiceManager)super.getServiceManager();
    }

    void init()
    {
        this.core.getServer().getScheduler().runTask(this.core, new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (BukkitModuleManager.this)
                {
                    for (Module module : getModules())
                    {
                        try
                        {
                            module.onStartupFinished();
                        }
                        catch (Exception ex)
                        {
                            module.getLog().warn(ex, "An uncaught exception occurred during onFinishLoading()");
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void verifyDependencies(ModuleInfo info) throws ModuleDependencyException
    {
        super.verifyDependencies(info);
        for (String plugin : info.getPluginDependencies())
        {
            if (this.pluginManager.getPlugin(plugin) == null)
            {
                throw new MissingPluginDependencyException(plugin);
            }
        }
    }
}
