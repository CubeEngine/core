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
import java.util.Locale;

import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.StringUtils;

public class Customcommands extends Module
{
    private CustomCommandsConfig config;

    @Override
    public void onEnable()
    {
        this.config = this.loadConfig(CustomCommandsConfig.class);

        if (this.config.commands.size() > 0)
        {
            this.getCore().getEventManager().registerListener(this, new CustomCommandsListener(this));
        }
        this.getCore().getCommandManager().registerCommands(this, new ManagementCommands(this), ReflectedCommand.class);
    }

    public CustomCommandsConfig getConfig()
    {
        return config;
    }

    public List<String> processMessage(String message)
    {
        String[] commands;
        List<String> messages = new ArrayList<String>();

        if (message.contains("!"))
        {
            commands = StringUtils.explode("!", message.substring(message.indexOf("!")), false);

            for (String command : commands)
            {
                String processedCommand = this.processCommand(command.toLowerCase(Locale.ENGLISH));
                if (!processedCommand.equals(""))
                {

                    messages.add(processedCommand);
                }
            }
        }
        return messages;
    }

    private String processCommand(String command)
    {
        int indexOfSpace = command.indexOf(" ");

        if (indexOfSpace > -1)
        {
            command = command.substring(0, indexOfSpace);
        }

        command = this.getConfig().commands.get(command);
        if (command != null)
        {
            return command;
        }
        return "";
    }
}
