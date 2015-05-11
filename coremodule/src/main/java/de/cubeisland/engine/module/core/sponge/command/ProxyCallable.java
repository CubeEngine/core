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
package de.cubeisland.engine.module.core.sponge.command;

import java.util.Collections;
import java.util.List;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;

import de.cubeisland.engine.module.core.command.CommandManager;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.CubeCommandDescriptor;
import de.cubeisland.engine.module.core.command.sender.BlockCommandSender;
import de.cubeisland.engine.module.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.module.core.sponge.SpongeCommandManager;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.user.UserManager;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.source.CommandBlockSource;
import org.spongepowered.api.util.command.source.ConsoleSource;

public class ProxyCallable implements CommandCallable
{
    private final CoreModule core;
    private final SpongeCommandManager manager;
    private final String alias;

    public ProxyCallable(CoreModule core, SpongeCommandManager manager, String alias)
    {
        this.core = core;
        this.manager = manager;
        this.alias = alias;
    }

    @Override
    public Optional<CommandResult> process(CommandSource source, String arguments) throws CommandException
    {
        try
        {
            long delta = System.currentTimeMillis();

            CommandSender wrapSender = wrapSender(source);
            boolean ran = manager.execute(newInvocation(wrapSender, alias + " " + arguments));

            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                core.getLog().warn("The following command used more than third a tick:\n   {} | {}ms ({}%)", arguments,
                                   delta, delta * 100 / (1000 / 20));
            }

            manager.logExecution(wrapSender, ran, alias, arguments);
            return Optional.absent();
        }
        catch (Exception e)
        {
            core.getLog().error(e, "An Unknown Exception occurred while executing a command! Command: {}",
                                alias + " " + arguments);
            return Optional.absent();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException
    {
        CommandSender wrapSender = wrapSender(source);
        List<String> suggestions = manager.getSuggestions(newInvocation(wrapSender, alias + " " + arguments));
        manager.logTabCompletion(wrapSender, alias, arguments);

        if (suggestions == null)
        {
            suggestions = Collections.emptyList();
        }
        Collections.sort(suggestions); // TODO put ? at the end
        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source)
    {
        CommandDescriptor descriptor = getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor)
        {
            Permission permission = ((CubeCommandDescriptor)descriptor).getPermission();
            if (permission != null)
            {
                source.hasPermission(permission.getFullName());
            }
        }
        return true;
    }

    private CommandDescriptor getDescriptor()
    {
        return manager.getCommand(alias).getDescriptor();
    }

    @Override
    public Optional<Text> getShortDescription(CommandSource source)
    {
        return Optional.of(Texts.of(getDescriptor().getDescription()));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source)
    {
        return Optional.absent(); // TODO
    }

    @Override
    public Text getUsage(CommandSource source)
    {
        return Texts.of(getDescriptor().getUsage(newInvocation(wrapSender(source), "")));
    }

    private CommandSender wrapSender(
        org.spongepowered.api.util.command.CommandSource spongeSender)
    {
        if (spongeSender instanceof CommandSender)
        {
            return (CommandSender)spongeSender;
        }
        else if (spongeSender instanceof Player)
        {
            return core.getModularity().start(UserManager.class).getExactUser(spongeSender.getName());
        }
        else if (spongeSender instanceof ConsoleSource)
        {
            return core.getModularity().start(CommandManager.class).getConsoleSender();
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

    private CommandInvocation newInvocation(de.cubeisland.engine.butler.CommandSource source, String commandLine)
    {
        return new CommandInvocation(source, commandLine, manager.getProviderManager());
    }

    public String getAlias()
    {
        return alias;
    }
}
