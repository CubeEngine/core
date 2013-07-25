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

import java.util.Collections;
import java.util.List;

import de.cubeisland.engine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.ChatFormat;



/**
 *
 * @author Phillip Schichtel
 */
public abstract class ContainerCommand extends ParameterizedCommand implements CommandHolder
{
    private static final List<String> NO_ALIASES = Collections.emptyList();
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

    public ContainerCommand(Module module, String name, String description, List<String> aliases)
    {
        this(module, ReflectedCommand.class, name, description, aliases);
    }

    public ContainerCommand(Module module, Class<? extends CubeCommand> subCommandType, String name, String description, List<String> aliases)
    {
        super(module, name, description, "[action]", aliases, new ParameterizedContextFactory(new ArgBounds(0)));
        this.subCommandType = subCommandType;
        this.delegation = null;
    }

    public void delegateChild(String name)
    {
        this.delegation = new ChildDelegation(name);
    }
    
    public void delegateChild(String name, ContextFilter filter)
    {
        this.delegation = new ChildDelegation(name, filter);
    }

    public Class<? extends CubeCommand> getCommandType()
    {
        return this.subCommandType;
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        if (this.delegation != null)
        {
            CubeCommand command = this.getChild(this.delegation.getChildName());
            if (command != null)
            {
                CommandContext childContext = command.getContextFactory().parse(command, context);
                childContext = this.delegation.getContextFilter().filterContext(childContext);
                return command.run(childContext);
            }

            this.getModule().getLog().warn("Child delegation failed: child '{}' not found!", this.delegation.getChildName());
        }

        this.help(new HelpContext(context));
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        CommandSender sender = context.getSender();
        context.sendTranslated("&7Usage: &f%s", this.getUsage(context));
        context.sendMessage(" ");
        context.sendTranslated("The following actions are available:");
        context.sendMessage(" ");

        for (CubeCommand command : context.getCommand().getChildren())
        {
            if (command.testPermissionSilent(sender))
            {
                context.sendMessage(ChatFormat.YELLOW + command.getName() + ChatFormat.WHITE + ": "  + ChatFormat.GREY + sender.translate(command.getDescription()));
            }
        }

        context.sendMessage(" ");
        context.sendTranslated("&7Detailed help: &9%s", "http://engine.cubeisland.de/commands/" + this.implodeCommandParentNames("/"));
    }

    private class ChildDelegation
    {
        private final String childName;
        private final ContextFilter contextFilter;

        private ChildDelegation(String childName)
        {
            this(childName, new ContextFilter() {
                @Override
                public CommandContext filterContext(CommandContext context)
                {
                    return context;
                }
            });
        }

        private ChildDelegation(String childName, ContextFilter contextFilter)
        {
            this.childName = childName;
            this.contextFilter = contextFilter;
        }

        public String getChildName()
        {
            return childName;
        }

        public ContextFilter getContextFilter()
        {
            return contextFilter;
        }
    }

    protected static interface ContextFilter
    {
        CommandContext filterContext(CommandContext context);
    }
}
