package de.cubeisland.cubeengine.core.module.exception;

//TODO DOCU
public class MissingPluginDependencyException extends ModuleException
{
    public MissingPluginDependencyException(String missing)
    {
        super("Missing plugin: " + missing);
    }
}
