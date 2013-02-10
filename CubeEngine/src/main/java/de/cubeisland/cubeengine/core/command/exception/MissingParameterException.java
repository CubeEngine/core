package de.cubeisland.cubeengine.core.command.exception;

public class MissingParameterException extends CommandException
{
    public MissingParameterException(String message)
    {
        super(message);
    }

    public MissingParameterException(Throwable cause)
    {
        super(cause);
    }

    public MissingParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
