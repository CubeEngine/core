package de.cubeisland.cubeengine.basics.moderation.kit;

class InvalidKitException extends Exception
{
    public InvalidKitException(String message)
    {
        super(message);
    }

    public InvalidKitException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
