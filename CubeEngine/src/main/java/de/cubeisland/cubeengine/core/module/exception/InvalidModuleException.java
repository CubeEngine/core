package de.cubeisland.cubeengine.core.module.exception;

//TODO DOCU
public class InvalidModuleException extends ModuleException
{
    private static final long serialVersionUID = 1530095056412810634L;

    public InvalidModuleException(String message)
    {
        super(message);
    }

    public InvalidModuleException(String message, Throwable cause)
    {
        super(message, cause);
    }
}