package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 *
 * @author Anselm Brehme
 */
public class CubeLogger extends Logger
{
    private boolean useBukkitLogger;
    private static Level loggingLevel = Level.ALL;

    /**
     * Creates a new Logger by this name
     *
     * @param name the name
     */
    public CubeLogger(String name)
    {
        this(name, true);
    }

    /**
     * Creates a new Logger by this name
     *
     * @param name the name
     * @param useBukkitLogger log into console or not
     */
    public CubeLogger(String name, boolean useBukkitLogger)
    {
        super(name, null);
        this.setParent(Logger.getLogger(name));
        this.setUseParentHandlers(false);
        this.setLevel(Level.ALL);
        this.useBukkitLogger = useBukkitLogger;
    }

    // Pass ConsoleLogging to BukkitLogger
    @Override
    public void log(LogRecord record)
    {
        String msg = record.getMessage();
        Object[] params = record.getParameters();
        if (!(params == null || params.length == 0))
        {
            Pattern.compile("\\{\\d").matcher(msg).find();
            msg = MessageFormat.format(msg, record.getParameters());
        }
        record.setParameters(null);

        if (!CubeEngine.getCore().isDebug())
        {
            record.setThrown(null);
        }
        Level level = record.getLevel();
        if (record.getLevel().intValue() <= Level.INFO.intValue())
        {
            record.setLevel(Level.INFO); // LogLevel lower than info are displayed as INFO anyways
        }
        if (useBukkitLogger)
        {
            if (level.intValue() >= loggingLevel.intValue())
            { // only log to console if Log is important enough
                record.setMessage("[" + this.getName() + "] " + msg);
                this.getParent().log(record);
                record.setMessage(msg);
            }
        }
        super.log(record);
    }

    public void exception(String msg, Throwable t)
    {
        this.log(Level.SEVERE, msg, t);
    }

    public void debug(Object msg)
    {
        if (CubeEngine.getCore().isDebug())
        {
            this.log(Level.INFO, "[Debug] {0}", msg);
        }
    }
    
    public void setUseBukkitLogger(boolean b)
    {
        this.useBukkitLogger = b;
    }
    
    public static void setLoggingLevel(Level level)
    {
        loggingLevel = level;
    }
}