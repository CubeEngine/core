package de.cubeisland.cubeengine.core.util.convert;

public class ConversionException extends Exception
{
    public ConversionException(String message)
    {
        super(message);
    }

    public ConversionException(Throwable cause)
    {
        super(cause);
    }

    public ConversionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}