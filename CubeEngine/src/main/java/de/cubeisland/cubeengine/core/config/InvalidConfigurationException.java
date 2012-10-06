package de.cubeisland.cubeengine.core.config;

/**
 *
 * @author Anselm Brehme
 */
public class InvalidConfigurationException extends RuntimeException
{
    private static final long serialVersionUID = -492268712863444129L;

    public InvalidConfigurationException(String message)
    {
        super(message);
    }
    
    public InvalidConfigurationException(String msg, Throwable t)
    {
        super(msg, t);
    }
}