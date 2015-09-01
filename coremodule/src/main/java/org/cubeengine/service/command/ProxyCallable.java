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
package org.cubeengine.service.command;

import java.util.Collections;
import java.util.List;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;

import org.cubeengine.service.command.property.RawPermission;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.command.sender.BlockCommandSender;
import org.cubeengine.service.command.sender.WrappedCommandSender;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.user.UserManager;
import org.spongepowered.api.entity.living.player.Player;
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
    public CommandResult process(CommandSource source, String arguments) throws CommandException
    {
        try
        {
            long delta = System.currentTimeMillis();

            CommandSender wrapSender = wrapSender(source);
            boolean ran = manager.execute(newInvocation(wrapSender, arguments.isEmpty() ? alias : alias + " " + arguments));

            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                core.getLog().warn("The following command used more than third a tick:\n   {} | {}ms ({}%)", arguments,
                                   delta, delta * 100 / (1000 / 20));
            }

            manager.logExecution(wrapSender, ran, alias, arguments);
            return CommandResult.success();
        }
        catch (Exception e)
        {
            core.getLog().error(e, "An Unknown Exception occurred while executing a command! Command: {}",
                                alias + " " + arguments);
            return CommandResult.empty();
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
        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source)
    {
        CommandDescriptor descriptor = getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor)
        {
            RawPermission permission = ((CubeCommandDescriptor)descriptor).getPermission();
            if (permission != null)
            {
                source.hasPermission(permission.getName());
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

    private CommandSender wrapSender(org.spongepowered.api.util.command.CommandSource spongeSender)
    {
        I18n i18n = core.getModularity().provide(I18n.class);
        if (spongeSender instanceof CommandSender)
        {
            return (CommandSender)spongeSender;
        }
        else if (spongeSender instanceof Player)
        {
            return core.getModularity().provide(UserManager.class).getExactUser(spongeSender.getName());
        }
        else if (spongeSender instanceof ConsoleSource)
        {
            return core.getModularity().provide(CommandManager.class).getConsoleSender();
        }
        else if (spongeSender instanceof CommandBlockSource)
        {
            return new BlockCommandSender(i18n, (CommandBlockSource)spongeSender);
        }
        else
        {
            return new WrappedCommandSender(i18n, spongeSender);
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
