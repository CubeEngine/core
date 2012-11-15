package de.cubeisland.cubeengine.core.storage;

/**
 * This exception is thrown when an error occurs during the storage-process.
 */
public class StorageException extends RuntimeException
{
    private static final long serialVersionUID = -3367478236326791833L;

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
