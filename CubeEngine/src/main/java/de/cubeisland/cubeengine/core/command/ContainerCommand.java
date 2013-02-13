package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedCommand;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class ContainerCommand extends ParameterizedCommand implements CommandHolder
{
    private static final List<String> NO_ALIASES = Collections.emptyList();
    private final Class<? extends CubeCommand> subCommandType;

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
        super(module, name, description, "[action]", aliases, new ParameterizedContextFactory());
        this.subCommandType = subCommandType;
    }

    public Class<? extends CubeCommand> getCommandType()
    {
        return this.subCommandType;
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        this.help(new HelpContext(context));
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {
        CommandSender sender = context.getSender();
        context.sendMessage("core", "Usage: " + this.getUsage(context));
        context.sendMessage(" ");
        context.sendMessage("core", "The following actions are available:");
        context.sendMessage(" ");

        for (CubeCommand command : context.getCommand().getChildren())
        {
            context.sendMessage(command.getName() + ": " + _(sender, command.getModule(), command.getDescription()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args)
    {
        if (args.length == 1 && !(!args[0].isEmpty() && args[0].charAt(0) == '-'))
        {
            List<String> actions = new ArrayList<String>();
            String token = args[0].toLowerCase(Locale.ENGLISH);
            for (String child : this.getChildrenNames())
            {
                if (child.startsWith(token))
                {
                    actions.add(child);
                }
            }
            Collections.sort(actions);

            return actions;
        }
        return super.tabComplete(sender, label, args);
    }
}
