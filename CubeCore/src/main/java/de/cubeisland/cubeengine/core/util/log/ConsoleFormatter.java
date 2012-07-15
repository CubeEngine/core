package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Anselm Brehme
 */
class ConsoleFormatter extends Formatter
{
    @Override
    public String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(record.getLoggerName());
        sb.append(" - ");
        sb.append(record.getMessage());
        return sb.toString();
    }
}
