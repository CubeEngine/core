package de.cubeisland.cubeengine.core.util.log;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * TODO review me
 *
 * @author Anselm Brehme
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
        super(name, null);
        Logger bukkitlogger = Logger.getLogger(name);
        this.setParent(bukkitlogger);
        this.setUseParentHandlers(false);
    }

    //Pass ConsoleLogging to BukkitLogger
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
        record.setMessage(this.getName() + " " + msg);
        this.getParent().log(record);
        record.setMessage(msg);
        super.log(record);
    }

    public void exception(String msg, Throwable t)
    {
        this.log(Level.SEVERE, msg, t);
    }
}