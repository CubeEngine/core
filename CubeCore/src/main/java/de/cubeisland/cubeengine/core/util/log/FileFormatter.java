package de.cubeisland.cubeengine.core.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author Robin Bechtel-Ostmann
 */
public class FileFormatter extends Formatter
{
    private static final String LINEBREAK = "\n";

    @Override
    public String format(LogRecord record)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        StringBuilder sb = new StringBuilder();
        sb.append(sdf.format(new Date(record.getMillis())));
        sb.append(" [");
        sb.append(record.getLevel().getLocalizedName().toUpperCase());
        sb.append("] ");
        sb.append(record.getLoggerName());
        sb.append(" - ");
        sb.append(record.getMessage());
        sb.append(LINEBREAK);
        Throwable throwIt = record.getThrown();
        if (throwIt != null)
        {
            sb.append("  ").append(throwIt.getMessage());
            sb.append(LINEBREAK);
        }
        return sb.toString();
    }
}
