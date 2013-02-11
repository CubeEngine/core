package de.cubeisland.cubeengine.core.logger;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ALL;

/**
 * This logger is used for all of CubeEngine's messages.
 */
public class CubeLogger extends Logger
{
    /**
     * Creates a new Logger by this name
     *
     * @param name the name
     */
    public CubeLogger(String name)
    {
        this(name, null);
    }

    /**
     * Creates a new Logger by this name
     *
     * @param name            the name
     * @param parent the parent logger
     */
    public CubeLogger(String name, Logger parent)
    {
        super(name, null);
        this.setLevel(ALL);
        if (parent != null)
        {
            this.setParent(parent);
        }
    }

    @Override
    public void log(LogRecord record)
    {
        if (this.getLevel().intValue() > LogLevel.DEBUG.intValue())
        {
            record.setThrown(null);
        }
        super.log(record);
    }
}
