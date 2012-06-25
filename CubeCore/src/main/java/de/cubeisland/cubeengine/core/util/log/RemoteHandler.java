package de.cubeisland.cubeengine.core.util.log;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 *
 * @author Faithcaio
 */
public class RemoteHandler extends Handler
{

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
