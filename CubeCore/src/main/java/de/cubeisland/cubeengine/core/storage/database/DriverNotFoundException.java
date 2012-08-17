package de.cubeisland.cubeengine.core.storage.database;

/**
 *
 * @author Phillip Schichtel
 */
public class DriverNotFoundException extends Exception
{
    public DriverNotFoundException(String message)
    {
        super(message);
    }
}
