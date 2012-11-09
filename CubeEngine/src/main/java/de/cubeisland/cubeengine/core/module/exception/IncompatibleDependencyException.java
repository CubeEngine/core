package de.cubeisland.cubeengine.core.module.exception;

/**
 * This exception is thrown when the dependency was found but its revision is not correct.
 */
public class IncompatibleDependencyException extends ModuleException
{
    public IncompatibleDependencyException(String module, String dep, int reqiredRev, int foundRev)
    {
        super("Module \"" + module + "\" requested revision " + reqiredRev + " of the module \"" + dep + "\" but found revision " + foundRev + "!");
    }
}
