package de.cubeisland.cubeengine.core.logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import static de.cubeisland.cubeengine.core.logger.LogLevel.ALL;
import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;
import java.util.logging.Level;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This logger is used for all of CubeEngine's messages.
 */
public class CubeLogger extends Logger
{
    private static Level loggingLevel = ALL;

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
        if (parent != null)
        {
            this.setParent(parent);
        }
        this.setLevel(ALL);
        this.setUseParentHandlers(false);
    }

    @Override
    public void log(LogRecord record)
    {
        Level level = record.getLevel();
        if (level.intValue() < LogLevel.INFO.intValue())
        {
            record.setLevel(INFO); // Lower LogLevel can get logged in Console too
        }
        if (!CubeEngine.isDebug())
        {
            record.setThrown(null);
        }
        super.log(record);
        if (this.getParent() != null)
        {
            if (level.intValue() > loggingLevel.intValue())
            {
                switch (record.getLevel().intValue())
                {
                    case 1000:
                        record.setLevel(SEVERE);
                        break;
                    case 900:
                        record.setLevel(WARNING);
                        break;
                    case 800:
                    case 700:
                    case 600:
                        record.setLevel(INFO);
                        break;
                }
                this.getParent().log(record);
            }
        }
    }

    public void exception(String msg, Throwable t)
    {
        this.log(LogLevel.ERROR, msg, t);
    }

    /**
     * This method logs a messages only if the CubeEngine is in debug mode
     *
     * @param msg the message
     */
    public void debug(String msg)
    {
        this.log(DEBUG, msg);
    }

    /**
     * This method logs a messages only if the CubeEngine is in debug mode
     *
     * @param msg the message
     */
    public void debug(String msg, Throwable t)
    {
        this.log(DEBUG, msg, t);
    }

    /**
     * This method sets the global logging level
     *
     * @param level the new logging level
     */
    public static void setLoggingLevel(Level level)
    {
        if (level != null)
        {
            loggingLevel = level;
        }
    }

    /**
     * This method returns the current logging level
     */
    public static Level getLoggingLevel()
    {
        return loggingLevel;
    }
}
