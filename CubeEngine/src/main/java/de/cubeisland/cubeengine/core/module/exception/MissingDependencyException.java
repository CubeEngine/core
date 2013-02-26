package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when a dependency is missing while loading a module.
 */
public class MissingDependencyException extends ModuleException
{
    public MissingDependencyException(String missing)
    {
        super("Missing module: " + missing);
    }
}
