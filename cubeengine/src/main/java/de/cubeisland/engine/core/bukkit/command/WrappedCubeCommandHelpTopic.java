package de.cubeisland.engine.core.bukkit.command;

import org.bukkit.command.CommandSender;
import org.bukkit.help.GenericCommandHelpTopic;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;

public class WrappedCubeCommandHelpTopic extends GenericCommandHelpTopic
{
    private final WrappedCubeCommand command;

    public WrappedCubeCommandHelpTopic(WrappedCubeCommand command)
    {
        super(command);
        this.command = command;
    }

    @Override
    public boolean canSee(CommandSender commandSender)
    {
        return false;
    }

    public WrappedCubeCommand getCommand()
    {
        return command;
    }

    public static final class Factory implements HelpTopicFactory<WrappedCubeCommand>
    {
        @Override
        public HelpTopic createTopic(WrappedCubeCommand command)
        {
            return new WrappedCubeCommandHelpTopic(command);
        }
    }
}
