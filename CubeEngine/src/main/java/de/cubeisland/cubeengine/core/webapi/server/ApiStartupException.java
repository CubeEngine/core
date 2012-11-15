package de.cubeisland.cubeengine.core.webapi.server;

class ApiStartupException extends Exception
{
    public ApiStartupException(String message, Throwable t)
    {
        super(message, t);
    }
}
