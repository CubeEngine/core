package de.cubeisland.cubeengine.core.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.ChatFormat;

import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;
import static de.cubeisland.cubeengine.core.util.StringUtils.startsWithIgnoreCase;

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

            this.getModule().getLog().log(WARNING, "Child delegation failed: child ''{0}'' not found!", this.delegation.getChildName());
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

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        if (args.length == 1 && !(!args[0].isEmpty() && args[0].charAt(0) == '-'))
        {
            List<String> actions = new ArrayList<String>();
            String token = args[0].toLowerCase(Locale.ENGLISH);

            Set<CubeCommand> names = this.getChildren();
            names.removeAll(this.childrenAliases);
            for (CubeCommand child : names)
            {
                if (startsWithIgnoreCase(child.getName(), token) && child.testPermissionSilent(sender))
                {
                    actions.add(child.getName());
                }
            }
            Collections.sort(actions);

            return actions;
        }
        return super.tabComplete(sender, label, args);
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
