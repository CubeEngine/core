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
package de.cubeisland.cubeengine.roles.role;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import de.cubeisland.cubeengine.core.module.event.FinishedLoadModulesEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserAuthorizedEvent;
import de.cubeisland.cubeengine.roles.RoleManager;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.RolesAttachment;
import de.cubeisland.cubeengine.roles.provider.WorldRoleProvider;

public class RolesEventHandler implements Listener
{
    private Roles module;
    private RoleManager roleManager;

    public RolesEventHandler(Roles module)
    {
        this.roleManager = module.getRoleManager();
        this.module = module;
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        this.roleManager.preCalculateRoles(event.getPlayer().getName(), false);
        long worldFromId = this.module.getCore().getWorldManager().getWorldId(event.getFrom());
        long worldToId = this.module.getCore().getWorldManager().getWorldId(event.getPlayer().getWorld());
        WorldRoleProvider fromProvider = this.roleManager.getProvider(worldFromId);
        WorldRoleProvider toProvider = this.roleManager.getProvider(worldToId);
        if (fromProvider.equals(toProvider))
        {
            if (toProvider.getWorlds().get(worldToId).getSecond())
            {
                return;
            }
        }
        this.roleManager.applyRole(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        this.roleManager.preCalculateRoles(event.getName(), false);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event)
    {
        this.roleManager.preCalculateRoles(event.getPlayer().getName(), false);
        this.roleManager.applyRole(event.getPlayer());
    }

    @EventHandler
    public void onAllModulesLoaded(FinishedLoadModulesEvent event)
    {
        this.roleManager.init();
        for (User user : this.module.getCore().getUserManager().getOnlineUsers()) // reapply roles on reload
        {
            user.get(RolesAttachment.class).removeRoleContainer(); // remove potential old calculated roles
            this.roleManager.preCalculateRoles(user.getName(), false);
            if (user.isOnline())
            {
                this.roleManager.applyRole(user.getPlayer());
            }
        }
    }

    @EventHandler
    public void onAuthorized(UserAuthorizedEvent event)
    {
        this.roleManager.reloadAllRolesAndApply(event.getUser(), event.getUser().getPlayer());
    }
}
