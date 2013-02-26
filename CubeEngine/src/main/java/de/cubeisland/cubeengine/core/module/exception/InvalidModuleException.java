package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when a module is considered as invalid.
 */
public class InvalidModuleException extends ModuleException
{
    public InvalidModuleException(String message)
    {
        super(message);
    }

    public InvalidModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
