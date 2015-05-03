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
package de.cubeisland.engine.core.sponge.command;

import java.util.Collections;
import java.util.List;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.alias.AliasDescriptor;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CubeCommandDescriptor;
import de.cubeisland.engine.core.command.CubeDescriptor;
import de.cubeisland.engine.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.source.CommandBlockSource;
import org.spongepowered.api.util.command.source.ConsoleSource;

public class WrappedCommand implements CommandCallable
{
    private final CommandBase command;
    private final Core core;

    public WrappedCommand(CommandBase command)
    {
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

    private CommandInvocation newInvocation(CommandSource source, String commandLine)
    {
        return new CommandInvocation(source, commandLine, core.getCommandManager().getProviderManager()).subInvocation(command);
    }

    private static de.cubeisland.engine.core.command.CommandSender wrapSender(Core core, org.spongepowered.api.util.command.CommandSource spongeSender)
    {
        if (spongeSender instanceof de.cubeisland.engine.core.command.CommandSender)
        {
            return (de.cubeisland.engine.core.command.CommandSender)spongeSender;
        }
        else if (spongeSender instanceof Player)
        {
            return core.getUserManager().getExactUser(spongeSender.getName());
        }
        else if (spongeSender instanceof ConsoleSource)
        {
            return core.getCommandManager().getConsoleSender();
        }
        else if (spongeSender instanceof CommandBlockSource)
        {
            return new BlockCommandSender(core, (CommandBlockSource)spongeSender);
        }
        else
        {
            return new WrappedCommandSender(core, spongeSender);
        }
    }


    @Override
    public Optional<CommandResult> process(org.spongepowered.api.util.command.CommandSource source,
                                           String arguments) throws CommandException
    {
        try
        {
            long delta = System.currentTimeMillis();
            CommandSource wrapSender = wrapSender(getModule().getCore(), source);
            boolean ran = this.command.execute(newInvocation(wrapSender, label, args));
            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                core.getLog().warn("The following command used more than third a tick:\n   {} {} | {}ms ({}%)", label, String.join(" ", args), delta, delta * 100 / (1000 / 20) );
            }
            core.getCommandManager().logExecution(wrapSender, ran, this.command, args);
            return ran;
        }
        catch (Exception e)
        {
            core.getLog().error(e, "An Unknown Exception occurred while executing a command! Command: {}", command.getDescriptor().getName());
            return false;
        }
    }

    @Override
    public List<String> getSuggestions(org.spongepowered.api.util.command.CommandSource source, String arguments) throws CommandException
    {
        CommandSource wrapSender = wrapSender(getModule().getCore(), source);
        CommandInvocation invocation = newInvocation(wrapSender, label, args);
        List<String> suggestions = this.command.getSuggestions(invocation);
        core.getCommandManager().logTabCompletion(wrapSender, command, args);
        if (suggestions == null)
        {
            suggestions = Collections.emptyList();
        }
        Collections.sort(suggestions);
        return suggestions;
    }

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
    public boolean testPermission(org.spongepowered.api.util.command.CommandSource source)
    {
        final String permission = this.getPermission();
        if ((permission == null) || (permission.isEmpty()))
        {
            return true;
        }
        return source.hasPermission(permission);
    }

    @Override
    public Optional<Text> getShortDescription(org.spongepowered.api.util.command.CommandSource source)
    {
        return Optional.of(Texts.of(command.getDescriptor().getDescription()));
    }

    @Override
    public Optional<Text> getHelp(org.spongepowered.api.util.command.CommandSource source)
    {
        return null;
    }

    @Override
    public Text getUsage(org.spongepowered.api.util.command.CommandSource source)
    {
        return Texts.of(command.getDescriptor().getUsage()); // TODO
    }
}
