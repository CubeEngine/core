package de.cubeisland.cubeengine.core.util.log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class is a file handler that logs into CubeEngine's log directory.
 */
public class CubeFileHandler extends FileHandler
{
    public CubeFileHandler(LogLevel level, String pattern) throws IOException, SecurityException
    {
        super(pattern + "_%g.log", 500000, 3, true);
        this.init(level);
    }

    public CubeFileHandler(LogLevel level, String pattern, boolean append) throws IOException, SecurityException
    {
        super(pattern + "_%g.log", 500000, 3, append);
        this.init(level);
    }

    public CubeFileHandler(LogLevel level, String pattern, int limit, int count) throws IOException, SecurityException
    {
        super(pattern + "_%g.log", limit, count, true);
        this.init(level);
    }

    public CubeFileHandler(LogLevel level, String pattern, int limit, int count, boolean append) throws IOException, SecurityException
    {
        super(pattern, limit, count, append);
        this.init(level);
    }

    private void init(LogLevel level) throws SecurityException, UnsupportedEncodingException
    {
        this.setFormatter(new FileFormatter());
        this.setEncoding("UTF-8");
        this.setLevel(level);
    }

    public class FileFormatter extends Formatter
    {
        private static final String LINEBREAK = "\n";
        private final SimpleDateFormat dateFormat;

        public FileFormatter()
        {
            this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        @Override
        public String format(LogRecord record)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(this.dateFormat.format(new Date(record.getMillis())));
            sb.append(" [");
            sb.append(record.getLevel().getLocalizedName().toUpperCase(Locale.ENGLISH));
            sb.append("] ");
            sb.append(record.getLoggerName());
            sb.append(" - ");
            sb.append(record.getMessage());
            sb.append(LINEBREAK);
            Throwable t = record.getThrown();
            if (t != null)
            {
                sb.append("  ").append(t.getMessage());
                sb.append(LINEBREAK);
            }
            return sb.toString();
        }
    }
}
