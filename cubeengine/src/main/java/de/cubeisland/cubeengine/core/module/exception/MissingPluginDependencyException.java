package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when a plugin dependency is missing while loading a module.
 */
public class MissingPluginDependencyException extends ModuleException
{
    public MissingPluginDependencyException(String missing)
    {
        super("Missing plugin: " + missing);
    }
}
