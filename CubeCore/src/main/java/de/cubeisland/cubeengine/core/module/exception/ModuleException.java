package de.cubeisland.cubeengine.core.module.exception;

/**
 *
 * @author Phillip Schichtel
 */
public class ModuleException extends Exception
{
    private static final long serialVersionUID = -4225079929008498906L;

    public ModuleException()
    {
        super();
    }

    public ModuleException(String message)
    {
        super(message);
    }

    public ModuleException(Throwable cause)
    {
        super(cause);
    }

    public ModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }
}