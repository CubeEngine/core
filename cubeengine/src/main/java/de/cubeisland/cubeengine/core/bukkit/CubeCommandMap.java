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
package de.cubeisland.cubeengine.core.bukkit;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;

/**
 * This CommandMap extends the SimpleCommandMap to add some functionality:
 * - an accessor for the known command map
 * - typo correction for the command lookup via edit distance
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core core;

    public CubeCommandMap(Core core, Server server, SimpleCommandMap oldMap)
    {
        super(server);
        this.core = core;
        for (Command command : oldMap.getCommands())
        {
            command.unregister(oldMap);
            this.register(command);
        }
    }

    /**
     * Returns a map of the known commands
     *
     * @return the known commands
     */
    public Map<String, Command> getKnownCommands()
    {
        return this.knownCommands;
    }

    @Override
    public Command getCommand(String name)
    {
        name = name.trim();
        if (name == null || name.isEmpty())
        {
            return null;
        }
        return super.getCommand(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Command getFallback(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return super.getFallback(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException
    {
        if (!CubeEngine.isMainThread())
        {
            throw new IllegalStateException("Commands may only be called synchronously!");
        }
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
        Command command = getCommand(label);

        if (command == null)
        {
            final Locale language = BukkitUtils.getLocaleFromSender(this.core.getI18n(), sender);
            List<String> matches = new LinkedList<String>(Match.string().getBestMatches(label, this.knownCommands.keySet(), 1));
            if (matches.size() > 0 && matches.size() <= this.core.getConfiguration().commandOffers)
            {
                Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
                if (matches.size() == 1)
                {
                    sender.sendMessage(this.core.getI18n().translate(language, "&cCouldn't find &e/%s&c. Did you mean &a/%s&c?", label, matches.iterator().next()));
                }
                else
                {
                    sender.sendMessage(this.core.getI18n().translate(language, "&eDid you mean one of these: &a%s &e?", "/" + StringUtils.implode(", /", matches)));
                }
                if (matches.size() > this.core.getConfiguration().commandTabCompleteOffers)
                {
                    matches = matches.subList(0, this.core.getConfiguration().commandTabCompleteOffers);
                }
            }
            else
            {
                sender.sendMessage(this.core.getI18n().translate(language, "&cI couldn't find any command for &e/%s &c...", label));
            }
            return false;
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
            // TODO we might catch errors here instead of on CubeCommand
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

    public boolean register(Command command)
    {
        return this.register(null, command);
    }

    protected synchronized boolean registerAndOverwrite(String label, String fallbackPrefix, Command command, boolean isAlias)
    {
        label = label.trim().toLowerCase();
        Command oldCommand = this.knownCommands.get(label);

        if (isAlias && oldCommand != null && !this.aliases.contains(label) && !(oldCommand instanceof CubeCommand))
        {
            // Request is for an alias and it conflicts with a existing command or previous alias ignore it
            // Note: This will mean it gets removed from the commands list of active aliases
            return false;
        }

        if (oldCommand != null && !this.aliases.contains(label))
        {
            String fallback = label;
            if (oldCommand instanceof PluginCommand)
            {
                fallback = ((PluginCommand)oldCommand).getPlugin().getName().toLowerCase(Locale.ENGLISH) + ":" + label;
            }
            else if (oldCommand instanceof BukkitCommand)
            {
                fallback = "bukkit:" + label;
            }
            else if (oldCommand instanceof CubeCommand)
            {
                fallback = ((CubeCommand)oldCommand).getModule().getId() + ":" + label;
                oldCommand.setLabel(fallback);
            }

            if (fallback != label)
            {
                this.knownCommands.remove(label);
                oldCommand.unregister(this);
                this.knownCommands.put(fallback, oldCommand);
                oldCommand.register(this);
            }
        }

        if (isAlias)
        {
            this.aliases.add(label);
        }
        else
        {
            // Ensure lowerLabel isn't listed as a alias anymore and update the commands registered name
            this.aliases.remove(label);
        }
        this.knownCommands.put(label, command);

        return true;
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command)
    {
        registerAndOverwrite(label, fallbackPrefix, command, false);

        for (String alias : command.getAliases())
        {
            registerAndOverwrite(alias, fallbackPrefix, command, true);
        }

        // Register to us so further updates of the commands label and aliases are postponed until its reregistered
        command.register(this);

        return true;
    }
}
