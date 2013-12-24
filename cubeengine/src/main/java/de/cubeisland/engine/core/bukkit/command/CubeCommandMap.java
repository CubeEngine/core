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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.matcher.Match;

/**
 * This CommandMap extends the SimpleCommandMap to add some functionality:
 * - typo correction for the command lookup via edit distance
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core core;

    public CubeCommandMap(BukkitCore core, SimpleCommandMap oldMap)
    {
        super(core.getServer());
        this.core = core;
        for (Command command : oldMap.getCommands())
        {
            command.unregister(oldMap);
            command.register(this);
        }
    }

    @Override
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException
    {
        commandLine = StringUtils.trimLeft(commandLine);
        if (commandLine.isEmpty())
        {
            return false;
        }

        String[] parts = commandLine.split(" ");

        if (parts.length == 0)
        {
            return false;
        }

        String label = parts[0].toLowerCase();
        Command command = getCommand(label.toLowerCase(Locale.ENGLISH));

        if (command == null)
        {
        }

        String[] args = null;
        // our commands expect spaces to be preserved
        if (command instanceof CubeCommand)
        {
            final int spaceIndex = commandLine.indexOf(' ');
            if (spaceIndex > -1 && spaceIndex + 1 < commandLine.length())
            {
                args = StringUtils.explode(" ", commandLine.substring(spaceIndex + 1));
            }
        }
        if (args == null)
        {
            args = Arrays.copyOfRange(parts, 1, parts.length);
        }

        try
        {
            command.execute(sender, label, args);
        }
        catch (CommandException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + command, e);
        }

        // return true as the command was handled
        return true;
    }
}
