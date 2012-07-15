package de.cubeisland.cubeengine.core.persistence.filesystem.config;

/**
 *
 * @author Anselm Brehme
 */
public class InvalidConfigurationException extends Exception
{
    InvalidConfigurationException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
