package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when modules have a circular dependency.
 */
public class CircularDependencyException extends ModuleException
{
    public CircularDependencyException(String dependingModule, String dependency)
    {
        super("The module '" + dependingModule
            + "' caused a circular dependency, because the module '" + dependency
            + "' is already on the loading stack!");
    }
}
