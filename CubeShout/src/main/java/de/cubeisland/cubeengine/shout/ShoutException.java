package de.cubeisland.cubeengine.shout;

public class ShoutException extends Exception
{
    private static final long serialVersionUID = 4325477695912520682L;

    public ShoutException()
    {
        super();
    }

    public ShoutException(String message)
    {
        super(message);
    }

    public ShoutException(Throwable cause)
    {
        super(cause);
    }

    public ShoutException(String message, Throwable cause)
    {
        super(message, cause);
    }
}