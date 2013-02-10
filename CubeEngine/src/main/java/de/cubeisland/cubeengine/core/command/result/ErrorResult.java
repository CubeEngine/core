package de.cubeisland.cubeengine.core.command.result;

import de.cubeisland.cubeengine.core.command.CommandResult;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;

public class ErrorResult implements CommandResult
{
    private final Exception exception;

    public ErrorResult(Exception exception)
    {
        this.exception = exception;
    }

    @Override
    public void show(CommandSender sender)
    {

    }
}
