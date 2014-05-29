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
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;

import static de.cubeisland.engine.core.util.ChatFormat.GREY;
import static de.cubeisland.engine.core.util.ChatFormat.WHITE;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;

public abstract class ContainerCommand extends CubeCommand implements CommandHolder
{
    private static final Set<String> NO_ALIASES = Collections.emptySet();
    private final Class<? extends CubeCommand> subCommandType;
    private DelegatingContextFilter delegation;

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
        super(module, name, description, new CubeContextFactory(), null, false);
        this.getContextFactory().addIndexed(CommandParameterIndexed.emptyIndex("action"));
        this.setAliases(aliases);
        this.subCommandType = subCommandType;
        this.delegation = null;
    }

    public void delegateChild(final String name)
    {
        this.delegation = new DelegatingContextFilter()
        {
            @Override
            public String delegateTo(CubeContext context)
            {
                return name;
            }
        };
    }
    
    public void delegateChild(DelegatingContextFilter filter)
    {
        this.delegation = filter;
    }

    public Class<? extends CubeCommand> getCommandType()
    {
        return this.subCommandType;
    }

    @Override
    public CommandResult run(CubeContext context)
    {
        return this.getChild("?").run(context);
    }

    public DelegatingContextFilter getDelegation()
    {
        return this.delegation;
    }

    @Override
    protected void addHelp()
    {
        this.addChild(new ContainerHelpCommand(this));
    }

    public static class ContainerHelpCommand extends HelpCommand
    {
        public ContainerHelpCommand(CubeCommand target)
        {
            super(target);
        }

        @Override
        public CommandResult run(CubeContext context)
        {
            CommandSender sender = context.getSender();
            context.sendTranslated(NONE, "{text:Usage:color=INDIGO}: {input#usage}", target.getUsage(context));
            context.sendMessage(" ");

            List<CubeCommand> commands = new ArrayList<>();
            for (CubeCommand child : target.getChildren())
            {
                if (child == this)
                {
                    continue;
                }
                if (!child.isCheckperm() || child.isAuthorized(sender))
                {
                    commands.add(child);
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
            context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + target.implodeCommandParentNames(
                "/"));
            return null;
        }
    }

    public static abstract class DelegatingContextFilter
    {
        public abstract String delegateTo(CubeContext context);
        public CubeContext filterContext(CubeContext context, String child)
        {
            return context;
        }
    }
}
