package de.cubeisland.cubeengine.core.command;

/**
 * This exception will be catched by the executor.
 * Its message will be send to the command sender.
 *
 * @author Phillip Schichtel
 */
public class CommandException extends RuntimeException
{
    public CommandException(String message)
    {
        super(message);
    }

    public CommandException(Throwable cause)
    {
        super(cause);
    }

    public CommandException(String message, Throwable cause)
    {
        super(message, cause);
    }
}