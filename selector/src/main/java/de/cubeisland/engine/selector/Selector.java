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
package de.cubeisland.engine.selector;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import de.cubeisland.engine.core.module.Module;

public class Selector extends Module implements Listener
{
    @Override
    public void onEnable()
    {
        this.getCore().getModuleManager().getServiceManager().registerService(this, de.cubeisland.engine.core.module.service.Selector.class, new CuboidSelector(this));
        try
        {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            this.getCore().getEventManager().registerListener(this, this); // only register if worldEdit is available
        }
        catch (ClassNotFoundException ignored)
        {
            this.getLog().warn("No WorldEdit found!");
        }
    }

    private boolean worldEditFound = false;

    @EventHandler
    public void onWorldEditEnable(PluginEnableEvent event)
    {
        if (event.getPlugin() instanceof WorldEditPlugin)
        {
            worldEditFound = true;
        }
    }

    public boolean hasWorldEdit()
    {
        return worldEditFound;
    }
}
