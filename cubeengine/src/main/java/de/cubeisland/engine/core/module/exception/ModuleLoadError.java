package de.cubeisland.engine.core.module.exception;

public class ModuleLoadError extends Error
{
    public ModuleLoadError()
    {
        super();
    }

    public ModuleLoadError(String message)
    {
        super(message);
    }

    public ModuleLoadError(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModuleLoadError(Throwable cause)
    {
        super(cause);
    }

    public ModuleLoadError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
