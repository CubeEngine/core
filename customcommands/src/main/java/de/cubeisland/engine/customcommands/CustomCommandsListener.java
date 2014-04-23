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
package de.cubeisland.engine.customcommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;
import static java.util.Locale.ENGLISH;

public class CustomCommandsListener implements Listener
{
    private final Customcommands customcommands;
    private final CustomCommandsConfig config;

    public CustomCommandsListener(Customcommands customcommands)
    {
        this.customcommands = customcommands;
        this.config = this.customcommands.getConfig();
    }

    @EventHandler
    public void onChat(ServerCommandEvent event)
    {
        handleMessages(event.getCommand(), event);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        handleMessages(event.getMessage(), event);
    }

    private void handleMessages(String message, Event event)
    {
        List<String> messages = processMessage(message);
        for (String currMessage : messages)
        {
            customcommands.getCore().getUserManager().broadcastMessage(NONE, currMessage);
        }
        if (config.surpressMessage && event instanceof Cancellable)
        {
            ((Cancellable)event).setCancelled(true);
        }
    }

    private List<String> processMessage(String message)
    {
        String[] commands;
        List<String> messages = new ArrayList<>();

        if (message.contains("!"))
        {
            commands = StringUtils.explode("!", message.substring(message.indexOf("!")), false);

            for (String command : commands)
            {
                command = command.toLowerCase(ENGLISH);
                int indexOfSpace = command.indexOf(" ");

                if (indexOfSpace > -1)
                {
                    command = command.substring(0, indexOfSpace);
                }

                command = config.commands.get(command);
                if (command == null || "".equals(command))
                {
                    continue;
                }

                messages.add(command);
            }
        }
        return messages;
    }
}
