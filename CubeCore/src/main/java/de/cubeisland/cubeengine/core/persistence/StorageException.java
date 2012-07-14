package de.cubeisland.cubeengine.core.persistence;

/**
 *
 * @author Phillip Schichtel
 */
public class StorageException extends RuntimeException
{
    public StorageException(String message)
    {
        super(message);
    }

    public StorageException(Throwable cause)
    {
        super(cause);
    }

    public StorageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
