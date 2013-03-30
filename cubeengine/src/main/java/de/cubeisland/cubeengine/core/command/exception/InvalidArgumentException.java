package de.cubeisland.cubeengine.core.command.exception;

public class InvalidArgumentException extends CommandException
{
    public InvalidArgumentException(String message)
    {
        super(message);
    }

    public InvalidArgumentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InvalidArgumentException(Throwable cause)
    {
        super(cause);
    }
}
