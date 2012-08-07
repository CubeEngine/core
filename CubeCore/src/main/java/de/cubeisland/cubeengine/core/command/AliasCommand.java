package de.cubeisland.cubeengine.core.command;

import java.util.List;

/**
 *
 * @author Phillip Schichtel
 */
public class AliasCommand extends CubeCommand
{
    private final CubeCommand command;

    public AliasCommand(String name, List<String> aliases, CubeCommand command)
    {
        super(command.getModule(), name, command.getDescription(), command.getUsage(), aliases);
        this.command = command;
    }

    @Override
    public void run(CommandContext context)
    {
        this.command.run(context);
    }

    @Override
    public void addSubCommand(CubeCommand command)
    {
        this.command.addSubCommand(command);
    }

    @Override
    public void removeSubCommand(String command)
    {
        this.command.removeSubCommand(command);
    }

    @Override
    public boolean hasSubCommand(String name)
    {
        return this.command.hasSubCommand(name);
    }
}