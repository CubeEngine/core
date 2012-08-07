package de.cubeisland.cubeengine.core.util.log;

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Phillip Schichtel
 */
public class ConsoleHandler extends java.util.logging.ConsoleHandler
{
    public ConsoleHandler(Level level, String format)
    {
        super();
        this.setFormatter(new ConsoleFormatter(format));
        this.setLevel(level);
    }
    
    public class ConsoleFormatter extends Formatter
    {
        private final String format;
        
        public ConsoleFormatter(String format)
        {
            this.format = format;
        }
        
        @Override
        public String format(LogRecord record)
        {
            return MessageFormat.format(this.format, record.getLoggerName(), record.getLevel().getName(), record.getMessage());
        }
    }
}
