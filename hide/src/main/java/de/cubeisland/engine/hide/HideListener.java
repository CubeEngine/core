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
import de.cubeisland.engine.hide.event.FakePlayerJoinEvent;
import de.cubeisland.engine.hide.event.FakePlayerQuitEvent;

public class HideListener implements Listener
{
    private final Hide module;
    private final UserManager um;

    public HideListener(Hide module)
    {
        this.module = module;
        this.um = module.getCore().getUserManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event)
    {
        final User joined = this.um.getExactUser(event.getPlayer().getName());
        if (event instanceof FakePlayerJoinEvent)
        {
            for (User canSeeHiddens : this.module.getCanSeeHiddens())
            {
                if (joined != canSeeHiddens)
                {
                    canSeeHiddens.sendTranslated("&aPlayer &e%s&a is now visible", joined.getDisplayName());
                }
            }
            return;
        }

        for (User hiddenUser : this.module.getHiddenUsers())
        {
            hiddenUser.sendMessage(event.getJoinMessage());
        }

        if (!this.module.getCanSeeHiddens().contains(joined))
        {
            for (User hiddenUser : this.module.getHiddenUsers())
            {
                joined.hidePlayer(hiddenUser);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void autoHide(PlayerJoinEvent event)
    {
        if (event instanceof FakePlayerJoinEvent)
        {
            return;
        }
        final User user = um.getExactUser(event.getPlayer().getName());
        if (HidePerm.AUTO_HIDE.isAuthorized(user))
        {
            event.setJoinMessage(null);
            for (Player current : um.getOnlineUsers())
            {
                if (!module.getCanSeeHiddens().contains(user))
                {
                    current.hidePlayer(user);
                }
            }
            this.module.getHiddenUsers().add(user);

            user.sendTranslated("&aYou were automatically hidden!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void autoSeehiddens(PlayerJoinEvent event)
    {
        final User user = um.getExactUser(event.getPlayer().getName());
        if (HidePerm.AUTO_SEEHIDDENS.isAuthorized(user) && !module.getCanSeeHiddens().contains(user))
        {
            module.getCanSeeHiddens().add(user);

            user.sendMessage("&aYou can automatically see hidden players!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event)
    {
        User player = um.getExactUser(event.getPlayer().getName());
        if (event instanceof FakePlayerQuitEvent)
        {
            for (User canSeeHiddens : this.module.getCanSeeHiddens())
            {
                if (user != canSeeHiddens)
                {
                    canSeeHiddens.sendTranslated("&aPlayer &e%s&a is now hidden!", user.getName());
                }
            }
            return;
        }

        if (event.getQuitMessage() != null)
        {
            Set<User> canSeeHiddensSet = this.module.getCanSeeHiddens();
            Set<User> hiddenUsers = this.module.getHiddenUsers();
            if (canSeeHiddensSet.contains(user))
            {
                for (User canSeeHiddens : canSeeHiddensSet)
                {
                    canSeeHiddens.sendMessage(event.getQuitMessage());
                }
                event.setQuitMessage(null);
                hiddenUsers.remove(user);
            }
            else
            {
                for (User hiddenUser : hiddenUsers)
                {
                    hiddenUser.sendMessage(event.getQuitMessage());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event)
    {

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPickupItem(PlayerPickupItemEvent event)
    {
        if (this.module.getCanSeeHiddens().contains(um.getExactUser(event.getPlayer().getName())))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        final Player player = event.getPlayer();
        Set<Player> recipients = event.getRecipients();
        if (this.module.getCanSeeHiddens().contains(um.getExactUser(player.getName())))
        {
            recipients.add(player);
        }
        else
        {
            for (User hiddenUser : this.module.getHiddenUsers())
            {
                recipients.add(hiddenUser.getPlayer());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTarget(EntityTargetEvent event)
    {
        final Entity entity = event.getEntity();
        if (entity instanceof Player)
        {
            if (this.module.getHiddenUsers().contains(um.getExactUser(((Player)entity).getName())))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDropItem(PlayerDropItemEvent event)
    {
        if (this.module.getHiddenUsers().contains(um.getExactUser(event.getPlayer().getName())) && !HidePerm.DROP.isAuthorized(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPressurePlate(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL && this.module.getHiddenUsers().contains(um.getExactUser(event.getPlayer().getName())))
        {
            event.setCancelled(true);
        }
    }
}
