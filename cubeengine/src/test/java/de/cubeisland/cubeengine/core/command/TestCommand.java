package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.module.Module;

public class TestCommand extends CubeCommand
{

    public TestCommand(Module module, String name, String description, ContextFactory contextFactory)
    {
        super(module, name, description, contextFactory);
    }

    @Override
    public CommandResult run(CommandContext context) throws Exception
    {
        return null;
    }

    @Override
    public void help(HelpContext context) throws Exception
    {}
}
