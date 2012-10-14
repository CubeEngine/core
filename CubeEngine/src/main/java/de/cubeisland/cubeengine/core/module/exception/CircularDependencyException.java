package de.cubeisland.cubeengine.core.module.exception;

//TODO DOCU
public class CircularDependencyException extends ModuleException
{
    private static final long serialVersionUID = -8766228903352920035L;

    public CircularDependencyException(String dependingModule, String dependency)
    {
        super("The module '" + dependingModule
            + "' caused a circular dependency, because the module '" + dependency
            + "' is already on the loading stack!");
    }
}