package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.Core;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This handler will post exceptions and other SEVERE messages to our log collector.
 */
public class RemoteHandler extends Handler
{
    private final Core core;

    public RemoteHandler(LogLevel level, Core core)
    {
        this.core = core;
        this.setLevel(level);
    }

    @Override
    public void publish(LogRecord record)
    {
        if (!isLoggable(record))
        {
            return;
        }
        //TODO API
    }

    @Override
    public void flush()
    {
    }

    @Override
    public void close() throws SecurityException
    {
    }
}