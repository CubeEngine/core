package de.cubeisland.engine.core.module.exception;

/**
 * Created with IntelliJ IDEA.
 * User: Phillip
 * Date: 22.11.13
 * Time: 00:32
 * To change this template use File | Settings | File Templates.
 */
public class ModuleDependencyException extends ModuleException
{
    public ModuleDependencyException()
    {
        super();
    }

    public ModuleDependencyException(String message)
    {
        super(message);
    }

    public ModuleDependencyException(Throwable cause)
    {
        super(cause);
    }

    public ModuleDependencyException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
