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
package de.cubeisland.cubeengine.core.bukkit.command;

import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import de.cubeisland.cubeengine.core.bukkit.BukkitCore;

public class FallbackCommandBackend extends SimpleCommandBackend
{
    private Listener commandListener;

    public FallbackCommandBackend(final BukkitCore core)
    {
        super(core, new SimpleCommandMap(core.getServer()));
        core.addInitHook(new Runnable() {
            @Override
            public void run()
            {
                core.getServer().getPluginManager().registerEvents(commandListener = new CommandListener(), core);
            }
        });
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        if (this.commandListener != null)
        {
            HandlerList.unregisterAll(this.commandListener);
            this.commandListener = null;
        }
    }

    private final class CommandListener implements Listener
    {
        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
        {
            final String message = event.getMessage().substring(1).trim();
            if (message.isEmpty())
            {
                return;
            }
            String[] parts = message.split(" ", 2);
            Command command = getCommand(parts[0].toLowerCase(Locale.ENGLISH));
            if (command != null)
            {
                dispatchCommand(core.getUserManager().getExactUser(event.getPlayer().getName()), message);
                event.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onServerCommand(ServerCommandEvent event)
        {
            final String message = event.getCommand().trim();
            if (message.isEmpty())
            {
                return;
            }
            String[] parts = message.split(" ", 2);
            Command command = getCommand(parts[0].toLowerCase(Locale.ENGLISH));
            if (command != null)
            {
                dispatchCommand(core.getCommandManager().getConsoleSender(), message);
            }
        }
    }
}
