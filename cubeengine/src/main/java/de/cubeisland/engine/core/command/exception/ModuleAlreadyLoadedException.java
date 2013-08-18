package de.cubeisland.engine.core.command.exception;

import de.cubeisland.engine.core.module.exception.ModuleException;

public class ModuleAlreadyLoadedException extends ModuleException
{
    public ModuleAlreadyLoadedException(String message)
    {
        super(message);
    }
}
