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
package de.cubeisland.engine.module.core.sponge;

import de.cubeisland.engine.module.core.module.BaseModuleManager;
import de.cubeisland.engine.module.core.module.Module;
import de.cubeisland.engine.module.core.module.ModuleInfo;
import de.cubeisland.engine.module.core.module.exception.MissingPluginDependencyException;
import de.cubeisland.engine.module.core.module.exception.ModuleDependencyException;
import org.spongepowered.api.plugin.PluginManager;

public class BukkitModuleManager extends BaseModuleManager
{
    private final SpongeCore core;
    private final PluginManager pluginManager;

    public BukkitModuleManager(SpongeCore core, ClassLoader parentClassLoader)
    {
        super(core, new SpongeServiceManager(core), new BukkitModuleLoader(core, parentClassLoader));
        this.pluginManager = core.getGame().getPluginManager();
        this.core = core;
    }

    @Override
    public SpongeServiceManager getServiceManager()
    {
        return (SpongeServiceManager)super.getServiceManager();
    }

    void init()
    {
        this.core.getTaskManager().runTask(this.core.getModuleManager().getCoreModule(), () -> {
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
        });
    }

    @Override
    protected void verifyDependencies(ModuleInfo info) throws ModuleDependencyException
    {
        super.verifyDependencies(info);
        for (String plugin : info.getPluginDependencies())
        {
            if (!this.pluginManager.getPlugin(plugin).isPresent())
            {
                throw new MissingPluginDependencyException(plugin);
            }
        }
    }
}
