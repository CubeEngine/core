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

public class CustomCommandsListener implements Listener
{
    private final Customcommands customcommands;

    public CustomCommandsListener(Customcommands customcommands)
    {
        this.customcommands = customcommands;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        String message = event.getMessage();

        while (message.contains("!"))
        {
            message = message.substring(message.indexOf("!"));
            event.getPlayer().sendMessage(processCommand(message));
        }
    }

    private String processCommand(String message)
    {
        String command = message.substring(0, message.indexOf(" "));

        if (command != null)
        {
            String text = customcommands.getConfig().commands.get(command);
            return text;
        }
        return null;
    }
}
