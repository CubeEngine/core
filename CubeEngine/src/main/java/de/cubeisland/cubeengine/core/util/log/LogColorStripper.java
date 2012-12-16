package de.cubeisland.cubeengine.core.util.log;


import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class LogColorStripper implements Filter
{
    @Override
    public boolean isLoggable(LogRecord record)
    {
        record.setMessage(record.getMessage().replaceAll("\u001B\\[((\\d\\d?;)?\\d\\d?)?m", ""));
        return true;
    }
}
