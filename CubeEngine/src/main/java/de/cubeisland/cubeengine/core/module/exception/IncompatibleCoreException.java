package de.cubeisland.cubeengine.core.module.exception;

import de.cubeisland.cubeengine.core.util.Version;

/**
 * This exception is thrown whenever the core revision is not compatible with a module.
 */
public class IncompatibleCoreException extends ModuleException
{
    public IncompatibleCoreException(String module, Version requiredVersion, Version actualVersion)
    {
        super("The module \"" + module + "\" required at least the core version " + requiredVersion + " but found " + actualVersion + "!");
    }
}
