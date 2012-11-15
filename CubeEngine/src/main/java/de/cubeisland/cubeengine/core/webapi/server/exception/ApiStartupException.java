package de.cubeisland.cubeengine.core.webapi.server.exception;

public class ApiStartupException extends Exception
{
    public ApiStartupException(String message, Throwable t)
    {
        super(message, t);
    }
}
