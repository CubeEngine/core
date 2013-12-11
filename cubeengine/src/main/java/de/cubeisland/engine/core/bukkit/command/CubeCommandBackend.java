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
package de.cubeisland.engine.core.bukkit.command;

import java.lang.reflect.Field;
import java.util.Collection;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.PluginManager;

import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.command.CubeCommand;


import static de.cubeisland.engine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.engine.core.util.ReflectionUtils.getFieldValue;

public class CubeCommandBackend extends SimpleCommandBackend
{
    private final BukkitCore core;
    private final Server server;
    private Listener commandPreprocessHook;
    private boolean warned;

    public CubeCommandBackend(final BukkitCore core)
    {
        super(core, swapCommandMap(core, null));
        this.core = core;
        this.server = core.getServer();
        this.commandPreprocessHook = new Listener() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void onCommandPreprocess(PlayerCommandPreprocessEvent event)
            {
                verifyCommandMap(event.getPlayer().getServer());
            }

            public void onServerCommand(ServerCommandEvent event)
            {
                verifyCommandMap(event.getSender().getServer());
            }
        };
        core.addInitHook(new Runnable() {
            @Override
            public void run()
            {
                server.getPluginManager().registerEvents(commandPreprocessHook, core);
            }
        });

        this.warned = false;
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
        this.resetCommandMap();
        HandlerList.unregisterAll(this.commandPreprocessHook);
    }

    private void verifyCommandMap(Server server)
    {
        final Field serverField = findFirstField(server, CommandMap.class);
        final SimpleCommandMap currentMap = getFieldValue(server, serverField, SimpleCommandMap.class);

        if (currentMap != this.getCommandMap())
        {
            if (!this.warned)
            {
                this.warned = true;
                this.core.getLog().warn("The server's CommandMap has changed since we replaced it!");
                this.core.getLog().warn("We won't be able to add new commands! Existing commands should still work though.");
            }
        }
    }

    @Override
    public void registerCommand(CubeCommand command)
    {
        this.verifyCommandMap(this.server);
        super.registerCommand(command);
    }

    private static SimpleCommandMap swapCommandMap(BukkitCore core, SimpleCommandMap newCommandMap)
    {
        final Server server = core.getServer();
        final PluginManager pm = server.getPluginManager();
        final Field serverField = findFirstField(server, CommandMap.class);
        final Field pmField = findFirstField(pm, CommandMap.class);

        if (newCommandMap == null)
        {
            newCommandMap = new CubeCommandMap(core, getFieldValue(server, serverField, SimpleCommandMap.class));
        }
        if (serverField != null && pmField != null)
        {
            try
            {
                serverField.set(server, newCommandMap);
                pmField.set(pm, newCommandMap);
            }
            catch (Exception ex)
            {
                core.getLog().debug(ex, "Failed to swap command map");
            }
        }
        return newCommandMap;
    }

    private void resetCommandMap()
    {
        SimpleCommandMap current = getFieldValue(core.getServer(), findFirstField(this.core.getServer(), CommandMap.class), SimpleCommandMap.class);
        if (current != null && current instanceof CubeCommandMap)
        {
            CubeCommandMap cubeMap = (CubeCommandMap)current;
            swapCommandMap(this.core, current = new SimpleCommandMap(this.core.getServer()));

            Collection<Command> commands = cubeMap.getCommands();

            for (Command command : commands)
            {
                command.unregister(cubeMap);
                if (command instanceof CubeCommand)
                {
                    continue;
                }
                String prefix = "";
                if (command instanceof PluginCommand)
                {
                    prefix = ((PluginCommand)command).getPlugin().getName();
                }
                else if (command instanceof BukkitCommand)
                {
                    prefix = "bukkit";
                }
                current.register(command.getLabel(), prefix, command);
            }
        }
    }
}
