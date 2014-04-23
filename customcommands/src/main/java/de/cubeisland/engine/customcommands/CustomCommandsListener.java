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

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

import de.cubeisland.engine.core.util.StringUtils;

public class CustomCommandsListener implements Listener
{
    private final Customcommands customcommands;

    public CustomCommandsListener(Customcommands customcommands)
    {
        this.customcommands = customcommands;
    }

    @EventHandler
    public void onChat(ServerCommandEvent event)
    {
        String message = event.getCommand();
        String[] commands;

        if (message.contains("!"))
        {
            commands = StringUtils.explode("!", message.substring(message.indexOf("!")));

            for (String command : commands)
            {
                String processedCommand = processCommand(command);
                if (processedCommand != "")
                {
                    event.getSender().sendMessage(processedCommand);
                }
            }
        }
    }

    private String processCommand(String message)
    {
        String command = message;
        int indexOfSpace = message.indexOf(" ");

        if (indexOfSpace > -1)
        {
            command = message.substring(0, indexOfSpace);
        }

        command = customcommands.getConfig().commands.get(command);
        if (command != null)
        {
            return command;
        }
        return "";
    }
}
