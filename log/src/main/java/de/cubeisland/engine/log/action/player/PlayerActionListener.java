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
package de.cubeisland.engine.log.action.player;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import de.cubeisland.engine.log.Log;

/**
 * A Listener for Player Actions
 * <p>Events:
 * {@link AsyncPlayerChatEvent}
 * {@link PlayerCommandPreprocessEvent}
 * {@link PlayerJoinEvent}
 * {@link PlayerQuitEvent}
 * {@link PlayerExpChangeEvent}
 * {@link PlayerTeleportEvent}
 * <p>Actions:
 * {@link PlayerChat}
 * {@link PlayerCommand}
 * {@link PlayerJoin}
 * {@link PlayerQuit}
 * {@link PlayerXp}
 * {@link PlayerTeleport}
 */
public class PlayerActionListener extends ListenerPlayerLog
{
    public PlayerActionListener(Log module)
    {
        super(module, PlayerChat.class, PlayerCommand.class, PlayerJoin.class, PlayerQuit.class, PlayerXp.class,
              PlayerTeleport.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        // TODO canceled chat?
        if (event.getMessage().trim().isEmpty())
        {
            return;
        }
        PlayerChat action = this.newAction(PlayerChat.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setMessage(event.getMessage());
            action.setMessageFormat(event.getFormat());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        // TODO permission check for base commands?
        if (event.getMessage().trim().isEmpty())
        {
            return;
        }
        PlayerCommand action = this.newAction(PlayerCommand.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setCommand(event.getMessage());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        PlayerJoin action = this.newAction(PlayerJoin.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            // TODO this.lm.getConfig(event.getPlayer().getWorld()).PLAYER_JOIN_ip
            // action.setIp(event.getPlayer().getAddress().getAddress().getHostAddress());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        PlayerQuit action = this.newAction(PlayerQuit.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setReason(event.getQuitMessage());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event)
    {
        PlayerXp action = this.newAction(PlayerXp.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setExp(event.getAmount());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event)
    {
        Location from = event.getFrom();
        Location to = event.getTo();
        PlayerTeleport action = this.newAction(PlayerTeleport.class, from.getWorld());
        if (action != null)
        {
            action.setLocation(from);
            action.setOtherLocation(to, true);
            action.setPlayer(event.getPlayer());
            this.logAction(action);
        }
        PlayerTeleport action2 = this.newAction(PlayerTeleport.class, to.getWorld());
        if (action2 != null)
        {
            action2.setLocation(to);
            action2.setOtherLocation(from, false);
            action2.setPlayer(event.getPlayer());
            this.logAction(action2);
        }
    }
}
