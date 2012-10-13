package de.cubeisland.cubeengine.core.util.log;

import de.cubeisland.cubeengine.core.Core;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Anselm Brehme
 */
public class RemoteHandler extends Handler
{
    private final Core core;

    public RemoteHandler(Level level, Core core)
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