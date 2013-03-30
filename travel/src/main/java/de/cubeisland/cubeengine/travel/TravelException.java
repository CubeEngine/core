package de.cubeisland.cubeengine.travel;

public class TravelException extends Exception
{
    public TravelException()
    {
        super();
    }

    public TravelException(String message)
    {
        super(message);
    }

    public TravelException(Throwable cause)
    {
        super(cause);
    }

    public TravelException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
