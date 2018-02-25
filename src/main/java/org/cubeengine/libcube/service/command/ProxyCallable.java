/*
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
package org.cubeengine.libcube.service.command;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import io.prometheus.client.Summary;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.libcube.service.MonitoringService;
import org.cubeengine.libcube.service.command.property.RawPermission;
import org.cubeengine.logscribe.Log;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ProxyCallable implements CommandCallable
{
    private final CubeCommandManager manager;
    private final Summary commandTimeSummary;
    private final String alias;
    private Log logger;

    public ProxyCallable(CubeCommandManager manager, Summary commandTimeSummary, String alias, Log logger)
    {
        this.manager = manager;
        this.commandTimeSummary = commandTimeSummary;
        this.alias = alias;
        this.logger = logger;
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException
    {
        try
        {

            CommandInvocation invocation = newInvocation(source, arguments.isEmpty() ? alias : alias + " " + arguments);

            long delta = System.currentTimeMillis();
            boolean ran;
            try (
                    Timing timing = Timings.ofStart(manager.getPlugin(), "CE Command Execute " + alias);
                    Summary.Timer t = commandTimeSummary.startTimer()
            )
            {
                ran = manager.execute(invocation);
            }

            delta = System.currentTimeMillis() - delta;
            if (delta > 1000 / 20 / 3) // third of a tick
            {
                logger.warn("Command Execute Timing: {} {} | {}ms ({}%)", this.alias, arguments,
                                   delta, delta * 100 / (1000 / 20));
            }

            manager.logExecution(source, ran, alias, arguments);
            return CommandResult.success();
        }
        catch (Exception e)
        {
            logger.error(e, "An Unknown Exception occurred while executing a command! Command: {}",
                                alias + " " + arguments);
            return CommandResult.empty();
        }
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException
    {
        // TODO maybe add targetPosition as Context in invocation? iirc there are a few cmds that use the block the player is looking at
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
                return source.hasPermission(permission.getName());
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
        return Optional.of(Text.of(getDescriptor().getDescription()));
    }

    @Override
    public Optional<Text> getHelp(CommandSource source)
    {
        return Optional.of(Text.of(getDescriptor().getUsage(null, alias)));
    }

    @Override
    public Text getUsage(CommandSource source)
    {
        return Text.of(getDescriptor().getUsage(newInvocation(source, "")));
    }

    private CommandInvocation newInvocation(CommandSource source, String commandLine)
    {
        return new CommandInvocation(source, commandLine, manager.getProviders());
    }

    public String getAlias()
    {
        return alias;
    }
}
