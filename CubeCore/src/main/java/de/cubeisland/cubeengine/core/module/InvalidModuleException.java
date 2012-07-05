package de.cubeisland.cubeengine.core.module;

/**
 *
 * @author Phillip Schichtel
 */
public class InvalidModuleException extends Exception
{
    public InvalidModuleException()
    {
        super();
    }

    public InvalidModuleException(String message)
    {
        super(message);
    }

    public InvalidModuleException(Throwable cause)
    {
        super(cause);
    }

    public InvalidModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
