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
package de.cubeisland.engine.horselocker;

import org.bukkit.entity.Horse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;

public class Horselocker extends Module implements Listener
{
    private Permission allowAccess;

    @Override
    public void onEnable()
    {
        this.getCore().getEventManager().registerListener(this, this);
        this.allowAccess = this.getBasePermission().createChild("allow-access");
        this.getCore().getPermissionManager().registerPermission(this, allowAccess);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked() instanceof Horse)
        {
            if (((Horse)event.getRightClicked()).isTamed())
            {
                if (event.getPlayer().equals(((Horse)event.getRightClicked()).getOwner()))
                {
                    return;
                }
                User user = this.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                if (!allowAccess.isAuthorized(user))
                {
                    user.sendTranslated("&cYou are not allowed to access this horse!");
                    event.setCancelled(true);
                }
            }
        }
    }
}
