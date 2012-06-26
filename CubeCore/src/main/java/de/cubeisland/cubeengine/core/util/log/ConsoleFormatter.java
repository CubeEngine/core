package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Faithcaio
 */
class ConsoleFormatter extends Formatter
{
    @Override
    public String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();
        Object[] params = record.getParameters();
        
        if (params != null && params.length > 0)
        {
            sb.append("[");
            sb.append(params[0]);
            sb.append("] - ");
        }
        sb.append(record.getMessage());
        return sb.toString();
    }
}
