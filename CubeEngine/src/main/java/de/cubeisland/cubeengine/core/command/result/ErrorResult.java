package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandResult;

public class ErrorResult implements CommandResult
{
    private final Exception exception;

    public ErrorResult(Exception exception)
    {
        this.exception = exception;
    }

    @Override
    public void show(CommandContext context)
    {
        context.sendMessage("core", "&cAn error occurred while running this command!");
    }
}
