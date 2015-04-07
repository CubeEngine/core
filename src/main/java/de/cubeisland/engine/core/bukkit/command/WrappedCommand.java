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
import java.util.Collections;
import java.util.List;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.alias.AliasConfiguration;
import de.cubeisland.engine.butler.alias.AliasDescriptor;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CubeCommandDescriptor;
import de.cubeisland.engine.core.command.CubeDescriptor;
import de.cubeisland.engine.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

public class WrappedCommand extends Command
{
    private final CommandBase command;
    private final Core core;
    private HelpTopic helpTopic;

    public WrappedCommand(CommandBase command)
    {
        super(command.getDescriptor().getName());
        this.command = command;
        CommandDescriptor descriptor = command.getDescriptor();
        if (descriptor instanceof AliasDescriptor)
        {
            descriptor = ((AliasDescriptor)descriptor).mainDescriptor();
        }
        this.core = ((CubeDescriptor)descriptor).getModule().getCore();
    }

    public Module getModule()
    {
        CommandDescriptor descriptor = command.getDescriptor();
        if (descriptor instanceof AliasDescriptor)
        {
            descriptor = ((AliasDescriptor)descriptor).mainDescriptor();
        }
        return ((CubeDescriptor)descriptor).getModule();
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
        List<String> aliases = new ArrayList<>();
        for (AliasConfiguration alias : this.command.getDescriptor().getAliases())
        {
            if (alias.getDispatcher() == null)
            {
                aliases.add(alias.getName());
            }
        }
        return aliases;
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
        if (this.command.getDescriptor() instanceof CubeCommandDescriptor)
        {
            Permission permission = ((CubeCommandDescriptor)this.command.getDescriptor()).getPermission();
            if (permission != null)
            {
                return permission.getFullName();
            }
        }
        return null;
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
            long delta = System.currentTimeMillis();
            CommandSource source = wrapSender(getModule().getCore(), sender);
            boolean ran = this.command.execute(newInvocation(source, label, args));
            core.getCommandManager().logExecution(source, ran, this.command, args);
            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                core.getLog().warn("The following command used more than third a tick:\n   {} {} | {}ms ({}%)", label, String.join(" ", args), delta, delta * 100 / (1000 / 20) );
            }
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

        return new CommandInvocation(source, commandLine, core.getCommandManager().getProviderManager()).subInvocation(command);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException
    {
        CommandSource source = wrapSender(getModule().getCore(), sender);
        CommandInvocation invocation = newInvocation(source, label, args);
        List<String> suggestions = this.command.getSuggestions(invocation);
        core.getCommandManager().logTabCompletion(source, command, args);
        if (suggestions == null)
        {
            suggestions = Collections.emptyList();
        }
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
