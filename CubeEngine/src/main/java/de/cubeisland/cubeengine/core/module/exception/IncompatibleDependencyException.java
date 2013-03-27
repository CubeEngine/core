package de.cubeisland.cubeengine.core.module.exception;

import de.cubeisland.cubeengine.core.util.Version;

/**
 * This exception is thrown when the dependency was found but its revision is not correct.
 */
public class IncompatibleDependencyException extends ModuleException
{
    public IncompatibleDependencyException(String module, String dep, Version requiredVersion, Version actualVersion)
    {
        super("Module \"" + module + "\" requested version " + requiredVersion + " of the module \"" + dep + "\" but found version " + actualVersion + "!");
    }
}
