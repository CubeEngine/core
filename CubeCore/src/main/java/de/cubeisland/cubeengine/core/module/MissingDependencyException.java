package de.cubeisland.cubeengine.core.module;

/**
 *
 * @author Phillip Schichtel
 */
public class MissingDependencyException extends Exception
{
    public MissingDependencyException(String missing)
    {
        super("Missing module: " + missing);
    }
}
