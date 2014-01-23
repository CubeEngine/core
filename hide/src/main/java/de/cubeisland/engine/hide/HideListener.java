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
package de.cubeisland.engine.hide;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.hide.event.UserHideEvent;
import de.cubeisland.engine.hide.event.UserShowEvent;

public class HideListener implements Listener
{
    private final Hide module;
    private final UserManager um;
    private final Set<String> hiddens;
    private final Set<String> canSeeHiddens;

    public HideListener(Hide module)
    {
        this.module = module;
        this.um = module.getCore().getUserManager();
        this.hiddens = module.getHiddenUsers();
        this.canSeeHiddens = module.getCanSeeHiddens();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event)
    {
        final String name = event.getPlayer().getName();
        final User joined = this.um.getExactUser(name);

        if (!canSeeHiddens.contains(name))
        {
            for (String hidden : hiddens)
            {
                joined.hidePlayer(um.getExactUser(hidden));
            }
        }
    }

    @EventHandler
    public void onShow(UserShowEvent event)
    {
        String name = event.getUser().getName();
        for (String canSeeHidden : canSeeHiddens)
        {
            if (!name.equals(canSeeHidden))
            {
                um.getExactUser(canSeeHidden).sendTranslated("&aPlayer &e%s&a is now visible", event.getUser()
                                                                                                     .getDisplayName());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void autoHide(PlayerJoinEvent event)
    {
        final String name = event.getPlayer().getName();
        final User user = um.getExactUser(name);
        if (module.perms().AUTO_HIDE.isAuthorized(user))
        {
            event.setJoinMessage(null);
            this.module.hidePlayer(user);

            user.sendTranslated("&aYou were automatically hidden!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void autoSeehiddens(PlayerJoinEvent event)
    {
        final String name = event.getPlayer().getName();
        final User user = um.getExactUser(name);
        if (module.perms().AUTO_SEEHIDDENS.isAuthorized(user) && !module.getCanSeeHiddens().contains(name))
        {
            canSeeHiddens.add(name);
            user.sendTranslated("&aYou can automatically see hidden players!");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        final String name = event.getPlayer().getName();

        if (event.getQuitMessage() != null)
        {
            if (hiddens.contains(name))
            {
                for (String canSeeHidden : canSeeHiddens)
                {
                    um.getExactUser(canSeeHidden).sendMessage(event.getQuitMessage());
                }
                event.setQuitMessage(null);
                hiddens.remove(name);
            }
        }
    }

    @EventHandler
    public void onHide(UserHideEvent event)
    {
        final String user = event.getUser().getName();
        for (String canSeeHidden : canSeeHiddens)
        {
            if (!user.equals(canSeeHidden))
            {
                um.getExactUser(canSeeHidden).sendTranslated("&aPlayer &e%s&a is now hidden!", event.getUser().getDisplayName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL && hiddens.contains(event.getPlayer().getName()) && !module.perms().INTERACT.isAuthorized(event.getPlayer()))
        {
            event.setCancelled(true);
            event.setUseInteractedBlock(Result.DENY);
            event.setUseItemInHand(Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(PlayerPickupItemEvent event)
    {
        if (hiddens.contains(event.getPlayer().getName()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (hiddens.contains(event.getPlayer().getName()) && !module.perms().CHAT.isAuthorized(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event)
    {
        final Entity entity = event.getEntity();
        if (entity instanceof Player)
        {
            if (hiddens.contains(((Player)entity).getName()))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent event)
    {
        if (hiddens.contains(event.getPlayer().getName()) && !module.perms().DROP.isAuthorized(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPressurePlate(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL && hiddens.contains(event.getPlayer().getName()))
        {
            event.setCancelled(true);
        }
    }
}
