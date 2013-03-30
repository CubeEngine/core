package de.cubeisland.cubeengine.core.bukkit;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class CommandLogFilter implements Filter
{
    private final Pattern DETECTION_PATTERN = Pattern.compile("[\\w\\d\\-\\.]{3,16} issued server command: /.+", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isLoggable(LogRecord record)
    {
        if (record.getLevel() == Level.INFO)
        {
            if (DETECTION_PATTERN.matcher(record.getMessage()).find())
            {
                return false;
            }
        }
        return true;
    }
}
