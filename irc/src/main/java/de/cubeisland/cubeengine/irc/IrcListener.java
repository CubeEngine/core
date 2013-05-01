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
package de.cubeisland.cubeengine.irc;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;

public class IrcListener implements Listener
{
    private final Irc irc;
    private final TaskManager taskmgr;
    private final BotManager botmgr;

    public IrcListener(Irc irc)
    {
        this.irc = irc;
        this.taskmgr = irc.getCore().getTaskManager();
        this.botmgr = irc.getBotManager();
    }

    public void onJoin(PlayerJoinEvent event)
    {
        this.botmgr.addPlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(final PlayerQuitEvent event)
    {
        this.taskmgr.runAsynchronousTask(this.irc, new Runnable()
        {
            @Override
            public void run()
            {
                if (!event.getPlayer().isOnline())
                {
                    botmgr.removePlayer(event.getPlayer());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event)
    {
        this.botmgr.sendMessage(event.getPlayer(), event.getMessage());
    }
}
