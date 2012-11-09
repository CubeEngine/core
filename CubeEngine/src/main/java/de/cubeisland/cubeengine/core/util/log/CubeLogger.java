package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This logger is used for all of CubeEngine's messages.
 */
public class CubeLogger extends Logger
{
    private static Level loggingLevel = Level.ALL;

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
     * @param useBukkitLogger log into console or not
     */
    public CubeLogger(String name, Logger parent)
    {
        super(name, null);
        if (parent != null)
        {
            this.setParent(parent);
        }
        this.setLevel(Level.ALL);
        this.setUseParentHandlers(false);
    }

    @Override
    public void log(LogRecord record)
    {
        Level level = record.getLevel();
        if (level.intValue() < Level.INFO.intValue())
        {
            record.setLevel(Level.INFO); // Lower LogLevel can get logged in Console too
        }
        if (this.getParent() != null)
        {
            if (level.intValue() > loggingLevel.intValue())
            {
                this.getParent().log(record);
            }
        }
        super.log(record);
    }

    public void exception(String msg, Throwable t)
    {
        this.log(Level.SEVERE, msg, t);
    }

    /**
     * This method logs a messages only if the CubeEngine is in debug mode
     *
     * @param msg the message
     */
    public void debug(Object msg)
    {
        if (CubeEngine.isDebug())
        {
            this.log(Level.INFO, "[Debug] " + msg);
        }
    }

    /**
     * This method sets the global logging level
     *
     * @param level the new logging level
     */
    public static void setLoggingLevel(Level level)
    {
        loggingLevel = level;
    }

    /**
     * This method returns the current logging level
     */
    public static Level getLoggingLevel()
    {
        return loggingLevel;
    }
}