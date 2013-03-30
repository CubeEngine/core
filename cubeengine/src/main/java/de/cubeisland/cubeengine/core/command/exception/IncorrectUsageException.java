package de.cubeisland.cubeengine.core.command.exception;

/**
 * This exception is thrown when a user performed an invalid command.
 * Use invalidUsage to throw an exception insinde a command. The exception will be caught.
 */
public class IncorrectUsageException extends CommandException
{
    public IncorrectUsageException()
    {
        super();
    }

    public IncorrectUsageException(String message)
    {
        super(message);
    }
}
