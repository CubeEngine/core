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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.completer.CompleterProviderProperty;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.command.property.PermissionProvider;
import de.cubeisland.engine.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.StringUtils;

public class WrappedCommand extends Command
{
    private final CommandBase command;
    private final Core core;
    private HelpTopic helpTopic;

    public WrappedCommand(CommandBase command, Core core)
    {
        super(command.getDescriptor().getName());
        this.command = command;
        this.core = core;
    }

    public Module getModule()
    {
        return this.command.getDescriptor().valueFor(ModuleProvider.class);
    }

    public CommandBase getCommand()
    {
        return command;
    }

    @Override
    public String getName()
    {
        return this.command.getDescriptor().getName();
    }

    @Override
    public boolean setLabel(String name)
    {
        // Not supported by our commands
        return false;
    }

    @Override
    public String getLabel()
    {
        return this.getName();
    }

    @Override
    public Command setDescription(String description)
    {
        // Not supported by our commands
        return this;
    }

    @Override
    public String getDescription()
    {
        return this.command.getDescriptor().getDescription();
    }

    @Override
    public Command setAliases(List<String> aliases)
    {
        // Not supported by our commands
        return this;
    }

    @Override
    public List<String> getAliases()
    {
        return new ArrayList<>(this.command.getDescriptor().getAliases());
    }

    @Override
    public Command setUsage(String usage)
    {
        // Not supported by our commands
        return this;
    }

    @Override
    public String getUsage()
    {
        return this.command.getDescriptor().getUsage(null);
    }

    @Override
    public String getPermission()
    {
        Permission permission = this.command.getDescriptor().valueFor(PermissionProvider.class);
        return permission == null ? null : permission.getFullName();
    }

    @Override
    public boolean testPermissionSilent(CommandSender target)
    {
        final String permission = this.getPermission();
        if ((permission == null) || (permission.length() == 0))
        {
            return true;
        }
        return target.hasPermission(permission);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args)
    {
        try
        {
            CommandSource source = wrapSender(getModule().getCore(), sender);
            boolean ran = this.command.execute(newInvocation(source, label, args));
            core.getCommandManager().logExecution(source, ran, this.command, args);
            return ran;
        }
        catch (Exception e)
        {
            core.getLog().error(e, "An Unknown Exception occurred while executing a command! Command: {}", command.getDescriptor().getName());
            return false;
        }
    }

    private CommandInvocation newInvocation(CommandSource source, String label, String[] args)
    {
        //this.command.getDescriptor().valueFor(DispatcherProperty.class).getDispatcher().getBaseDispatcher()
        String commandLine = label;
        if (args.length > 0)
        {
            commandLine += " " + StringUtils.implode(" ", args);
        }
        CommandInvocation invocation = new CommandInvocation(source, commandLine, getModule().getCore().getCommandManager().getReaderManager()).subInvocation();
        invocation.setProperty(new CompleterProviderProperty(core.getCommandManager()));
        return invocation;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException
    {
        CommandSource source = wrapSender(getModule().getCore(), sender);
        CommandInvocation invocation = newInvocation(source, label, args);
        List<String> suggestions = this.command.getSuggestions(invocation);
        core.getCommandManager().logTabCompletion(source, command, args);
        Collections.sort(suggestions);
        return suggestions;
    }

    public HelpTopic getHelpTopic()
    {
        return helpTopic;
    }

    public void setHelpTopic(HelpTopic helpTopic)
    {
        this.helpTopic = helpTopic;
    }

    private static de.cubeisland.engine.core.command.CommandSender wrapSender(Core core, org.bukkit.command.CommandSender bukkitSender)
    {
        if (bukkitSender instanceof de.cubeisland.engine.core.command.CommandSender)
        {
            return (de.cubeisland.engine.core.command.CommandSender)bukkitSender;
        }
        else if (bukkitSender instanceof Player)
        {
            return core.getUserManager().getExactUser(bukkitSender.getName());
        }
        else if (bukkitSender instanceof org.bukkit.command.ConsoleCommandSender)
        {
            return core.getCommandManager().getConsoleSender();
        }
        else if (bukkitSender instanceof org.bukkit.command.BlockCommandSender)
        {
            return new BlockCommandSender(core, (org.bukkit.command.BlockCommandSender)bukkitSender);
        }
        else
        {
            return new WrappedCommandSender(core, bukkitSender);
        }
    }
}
