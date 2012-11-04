package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when a dependency is missing while loading a module.
 */
public class MissingDependencyException extends ModuleException
{
    private static final long serialVersionUID = 3174710484793092922L;

    public MissingDependencyException(String missing)
    {
        super("Missing module: " + missing);
    }
}