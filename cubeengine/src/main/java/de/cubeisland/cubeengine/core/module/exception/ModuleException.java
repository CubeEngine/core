package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is fired when an error occurs during the loading-process of modules.
 */
public class ModuleException extends Exception
{
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