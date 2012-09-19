package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Anselm Brehme
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
     * @param name the name
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
    }

    @Override
    public void log(LogRecord record)
    {
        if (this.getParent() != null)
        {
            getParent().log(record);
        }
        else
        {
            super.log(record);
        }
    }

    public void exception(String msg, Throwable t)
    {
        this.log(Level.SEVERE, msg, t);
    }

    public void debug(Object msg)
    {
        if (CubeEngine.isDebug())
        {
            this.log(Level.INFO, "[Debug] " + msg);
        }
    }
    
    public static void setLoggingLevel(Level level)
    {
        loggingLevel = level;
    }
}