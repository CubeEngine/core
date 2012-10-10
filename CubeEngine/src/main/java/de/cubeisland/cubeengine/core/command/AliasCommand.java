package de.cubeisland.cubeengine.core.command;

import java.util.List;

/**
 * This class is a simple alias command the revers to a single command on ANY level
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
    public void run(CommandContext context) throws Exception
    {
        this.command.run(context);
    }

    @Override
    public void addChild(CubeCommand command)
    {
        this.command.addChild(command);
    }

    @Override
    public void removeChild(String command)
    {
        this.command.removeChild(command);
    }

    @Override
    public boolean hasChild(String name)
    {
        return this.command.hasChild(name);
    }
    
    @Override
    public boolean hasChildren()
    {
        return this.command.hasChildren();
    }

    @Override
    public void showHelp(CommandContext context) throws Exception
    {
        this.command.showHelp(context);
    }
}