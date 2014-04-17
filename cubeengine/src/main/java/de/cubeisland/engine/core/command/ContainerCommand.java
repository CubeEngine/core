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
package de.cubeisland.engine.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;
import de.cubeisland.engine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;

import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public abstract class ContainerCommand extends ParameterizedCommand implements CommandHolder
{
    private static final Set<String> NO_ALIASES = Collections.emptySet();
    private final Class<? extends CubeCommand> subCommandType;
    private ChildDelegation delegation;

    public ContainerCommand(Module module, String name, String description)
    {
        this(module, ReflectedCommand.class, name, description, NO_ALIASES);
    }

    public ContainerCommand(Module module, Class<? extends CubeCommand> subCommandType, String name, String description)
    {
        this(module, subCommandType, name, description, NO_ALIASES);
    }

    public ContainerCommand(Module module, String name, String description, Set<String> aliases)
    {
        this(module, ReflectedCommand.class, name, description, aliases);
    }

    public ContainerCommand(Module module, Class<? extends CubeCommand> subCommandType, String name, String description, Set<String> aliases)
    {
        super(module, name, description, new ParameterizedContextFactory(CommandParameterIndexed.emptyIndex("action")), null);
        this.setAliases(aliases);
        this.subCommandType = subCommandType;
        this.delegation = null;
    }

    public void delegateChild(String name)
    {
        this.delegation = new ChildDelegation(name);
    }
    
    public void delegateChild(ContextFilter filter)
    {
        this.delegation = new ChildDelegation(filter);
    }

    public Class<? extends CubeCommand> getCommandType()
    {
        return this.subCommandType;
    }

    @Override
    public CommandResult run(CommandContext context)
    {
        this.help(new HelpContext(context));
        return null;
    }

    public ChildDelegation getDelegation()
    {
        return this.delegation;
    }

    @Override
    public void help(HelpContext context)
    {
        CommandSender sender = context.getSender();
        context.sendTranslated(NONE, "{text:Usage:color=INDIGO}: {input#usage}", this.getUsage(context));
        context.sendMessage(" ");

        List<CubeCommand> commands = new ArrayList<>();
        for (CubeCommand command : context.getCommand().getChildren())
        {
            if (command.isAuthorized(sender))
            {
                commands.add(command);
            }
        }

        if (commands.isEmpty())
        {
            context.sendTranslated(NEGATIVE, "No actions are available");
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The following actions are available:");
            context.sendMessage(" ");
            for (CubeCommand command : commands)
            {
                context.sendMessage(YELLOW + command.getName() + WHITE + ": "  + GREY + sender.getTranslation(NONE, command.getDescription()));
            }
        }
        context.sendMessage(" ");
        context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + this.implodeCommandParentNames("/"));
    }

    public static class ChildDelegation
    {
        private final ContextFilter contextFilter;

        private ChildDelegation(final String childName)
        {
            this(new ContextFilter() {

                @Override
                public String delegateTo(CommandContext context)
                {
                    return childName;
                }
            });
        }

        private ChildDelegation(ContextFilter contextFilter)
        {
            this.contextFilter = contextFilter;
        }

        public String delegateTo(CommandContext context)
        {
            return contextFilter.delegateTo(context);
        }

        public CommandContext filterContext(CommandContext context, String child)
        {
            return this.contextFilter.filterContext(context, child);
        }
    }

    protected static abstract class ContextFilter
    {
        public abstract String delegateTo(CommandContext context);
        public CommandContext filterContext(CommandContext context, String child)
        {
            return context;
        }
    }
}
