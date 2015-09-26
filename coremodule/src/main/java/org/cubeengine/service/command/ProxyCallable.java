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
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.command.property.RawPermission;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

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

            boolean ran = manager.execute(newInvocation(source, arguments.isEmpty() ? alias : alias + " " + arguments));

            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                core.getLog().warn("The following command used more than third a tick:\n   {} | {}ms ({}%)", arguments,
                                   delta, delta * 100 / (1000 / 20));
            }

            manager.logExecution(source, ran, alias, arguments);
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
        List<String> suggestions = manager.getSuggestions(newInvocation(source, alias + " " + arguments));
        manager.logTabCompletion(source, alias, arguments);

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
        return Texts.of(getDescriptor().getUsage(newInvocation(source, "")));
    }

    private CommandInvocation newInvocation(CommandSource source, String commandLine)
    {
        return new CommandInvocation(source, commandLine, manager.getProviderManager());
    }

    public String getAlias()
    {
        return alias;
    }
}
