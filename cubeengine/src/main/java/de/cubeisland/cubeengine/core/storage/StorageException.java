package de.cubeisland.cubeengine.core.storage;

import java.sql.SQLException;
import java.sql.Statement;

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

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Also prints the SQL-Statement that caused the exception
     *
     * @param message
     * @param cause
     * @param statement
     */
    public StorageException(String message, SQLException cause, Statement statement)
    {
        //this allows seeing the complete sql that did produce the error
        super(message + "\n\n" + statement.toString(), cause);
    }
}
