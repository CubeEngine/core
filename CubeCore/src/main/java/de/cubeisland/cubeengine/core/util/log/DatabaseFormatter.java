package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Faithcaio
 */
public class DatabaseFormatter extends Formatter
{
    public String format(LogRecord rec)
    {
        //TODO
        return "";
    }

    private String calcDate(long millisecs)
    {
        return "TODO"; //TODO
    }

    // This method is called just after the handler using this
    // formatter is created
    @Override
    public String getHead(Handler h)
    {
        //TODO create table if not exist here
        return "";
    }

    // This method is called just after the handler using this
    // formatter is closed
    @Override
    public String getTail(Handler h)
    {
        //TODO needed?
        return "";
    }
}
