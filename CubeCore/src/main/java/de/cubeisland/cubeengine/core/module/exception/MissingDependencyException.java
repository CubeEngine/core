package de.cubeisland.cubeengine.core.module.exception;

/**
 *
 * @author Phillip Schichtel
 */
public class MissingDependencyException extends ModuleException
{
    private static final long serialVersionUID = 3174710484793092922L;

    public MissingDependencyException(String missing)
    {
        super("Missing module: " + missing);
    }
}