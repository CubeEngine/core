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
package de.cubeisland.cubeengine.basics.command.general;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.BasicsPerm;

public class AfkListener implements Listener, Runnable
{
    private final Basics basics;
    private final UserManager um;
    private final long autoAfk;
    private final long afkCheck;

    public AfkListener(Basics basics, long autoAfk, long afkCheck)
    {
        this.basics = basics;
        this.um = basics.getCore().getUserManager();
        this.autoAfk = autoAfk;
        this.afkCheck = afkCheck;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event)
    {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ())
        {
            return;
        }
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            this.updateLastAction((Player)event.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerInteract(PlayerInteractEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        this.updateLastAction(event.getPlayer());
        this.run();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatTabComplete(PlayerChatTabCompleteEvent event)
    {
        this.updateLastAction(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event)
    {
        BasicsAttachment basicsAttachment = this.um.getExactUser(event.getPlayer()).get(BasicsAttachment.class);
        if (basicsAttachment != null)
        {
            basicsAttachment.setAfk(false);
            basicsAttachment.resetLastAction();
        }
    }

    private void updateLastAction(Player player)
    {
        BasicsAttachment basicsAttachment = this.um.getExactUser(player).get(BasicsAttachment.class);
        if (basicsAttachment != null)
        {
            if (basicsAttachment.isAfk() && BasicsPerm.PREVENT_AUTOUNAFK.isAuthorized(player))
            {
                return;
            }
            basicsAttachment.updateLastAction();
        }
    }

    @Override
    public void run()
    {
        BasicsAttachment basicsAttachment;
        for (User user : this.basics.getCore().getUserManager().getLoadedUsers())
        {
            if (user.isOnline())
            {
                basicsAttachment = user.attachOrGet(BasicsAttachment.class, this.basics);
                long lastAction = basicsAttachment.getLastAction();
                if (lastAction == 0)
                {
                    continue;
                }
                if (basicsAttachment.isAfk())
                {
                    if (System.currentTimeMillis() - lastAction < this.afkCheck)
                    {
                        basicsAttachment.setAfk(false);
                        this.um.broadcastStatus("is no longer afk!", user);
                    }
                }
                else if (System.currentTimeMillis() - lastAction > this.autoAfk)
                {
                    if (!BasicsPerm.PREVENT_AUTOAFK.isAuthorized(user))
                    {
                        basicsAttachment.setAfk(true);
                        this.um.broadcastStatus("is now afk!" ,user);
                    }
                }
            }
        }
    }
}
