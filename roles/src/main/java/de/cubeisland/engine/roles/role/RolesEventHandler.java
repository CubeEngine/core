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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserAuthorizedEvent;
import de.cubeisland.engine.roles.Roles;

public class RolesEventHandler implements Listener
{
    private final Roles module;
    private final RolesManager rolesManager;

    public RolesEventHandler(Roles module)
    {
        this.module = module;
        this.rolesManager = module.getRolesManager();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event)
    {
        WorldRoleProvider fromProvider = this.rolesManager.getProvider(event.getFrom());
        WorldRoleProvider toProvider = this.rolesManager.getProvider(event.getPlayer().getWorld());
        if (fromProvider.equals(toProvider))
        {
            if (toProvider.getWorldMirrors().get(event.getPlayer().getWorld()).getSecond()
            && fromProvider.getWorldMirrors().get(event.getFrom()).getSecond())
            {
                return;
            }
        }
        this.rolesManager.getRolesAttachment(event.getPlayer()).getCurrentDataHolder().apply();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event)
    {
        User user = this.module.getCore().getUserManager().findUser(event.getName());
        if (user != null && (user.hasPlayedBefore() || user.isOnline())) // prevent NPE for players that never joined the server
        {
            if (user.getWorld() != null) // prevent NPE for players on deleted worlds
            {
                user.attachOrGet(RolesAttachment.class, this.module).getDataHolder(user.getWorld()); // Pre-calculate
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event)
    {
        if (event.getResult().equals(Result.ALLOWED)) // only if allowed to join
        {
            this.rolesManager.getRolesAttachment(event.getPlayer()).getCurrentDataHolder(); // Pre-calculate
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event)
    {
        this.rolesManager.getRolesAttachment(event.getPlayer()).getCurrentDataHolder().apply(); // Pre-calculate + apply
    }

    @EventHandler
    public void onAuthorized(UserAuthorizedEvent event)
    {
        RolesAttachment rolesAttachment = this.rolesManager.getRolesAttachment(event.getUser());
        rolesAttachment.flushData();
        rolesAttachment.getCurrentDataHolder().apply(); // Pre-Calculate + apply
    }
}
