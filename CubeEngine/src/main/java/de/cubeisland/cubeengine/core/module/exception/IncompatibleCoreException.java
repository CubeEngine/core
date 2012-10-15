package de.cubeisland.cubeengine.core.module.exception;

import de.cubeisland.cubeengine.core.Core;

/**
 * This exception is thrown whenever the core revision is not compatible with a module.
 */
public class IncompatibleCoreException extends ModuleException
{
    public IncompatibleCoreException(String module, int requiredRev)
    {
        super("The module \"" + module + "\" required at least the core revision " + requiredRev + " but found " + Core.REVISION + "!");
    }
}
