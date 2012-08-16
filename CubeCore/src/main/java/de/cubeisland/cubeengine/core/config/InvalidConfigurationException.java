package de.cubeisland.cubeengine.core.config;

/**
 *
 * @author Anselm Brehme
 */
public class InvalidConfigurationException extends Exception
{
    private static final long serialVersionUID = -492268712863444129L;

    InvalidConfigurationException(String msg, Throwable t)
    {
        super(msg, t);
    }
}