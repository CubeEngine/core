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
package de.cubeisland.engine.roles.role;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserAuthorizedEvent;
import de.cubeisland.engine.roles.Roles;

public class RolesEventHandler implements Listener
{
    private Roles module;
    private RolesManager rolesManager;

    public RolesEventHandler(Roles module)
    {
        this.rolesManager = module.getRolesManager();
        this.module = module;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        long worldFromId = this.module.getCore().getWorldManager().getWorldId(event.getFrom());
        long worldToId = this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld());
        WorldRoleProvider fromProvider = this.rolesManager.getProvider(worldFromId);
        WorldRoleProvider toProvider = this.rolesManager.getProvider(worldToId);
        if (fromProvider.equals(toProvider))
        {
            if (toProvider.getWorldMirrors().get(worldToId).getSecond() && fromProvider.getWorldMirrors().get(worldFromId).getSecond())
            {
                return;
            }
        }
        RolesAttachment rolesAttachment = this.rolesManager.getRolesAttachment(event.getPlayer());
        rolesAttachment.apply();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        User user = this.module.getCore().getUserManager().findUser(event.getName());
        if (user != null && (user.hasPlayedBefore() || user.isOnline())) // prevent NPE for players that are banned but never joined the server
        {
            RolesAttachment rolesAttachment = this.rolesManager.getRolesAttachment(user);
            rolesAttachment.getResolvedData(user.getWorldId()); // Pre-calculate
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event)
    {
        if (event.getResult().equals(Result.ALLOWED)) // only if allowed to join
        {
            final RolesAttachment rolesAttachment = this.rolesManager.getRolesAttachment(event.getPlayer());
            rolesAttachment.getResolvedData(this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld())); // Pre-calculate
            this.module.getCore().getTaskManager().runTask(this.module, new Runnable()
            {
                @Override
                public void run()
                {
                    rolesAttachment.apply(); // apply once joined
                }
            });
        }
    }

    @EventHandler
    public void onAuthorized(UserAuthorizedEvent event)
    {
        RolesAttachment rolesAttachment = this.rolesManager.getRolesAttachment(event.getUser());
        rolesAttachment.flushResolvedData();
        rolesAttachment.getCurrentResolvedData(); // Pre-calculate
        rolesAttachment.apply();
    }
}
