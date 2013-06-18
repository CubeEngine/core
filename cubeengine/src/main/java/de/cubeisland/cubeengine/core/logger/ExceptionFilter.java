package de.cubeisland.cubeengine.core.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class ExceptionFilter extends Filter<ILoggingEvent>
{
    @Override
    public FilterReply decide(ILoggingEvent event)
    {
        if (event.getThrowableProxy() != null)
        {
            return FilterReply.ACCEPT;
        }
        else
        {
            return FilterReply.DENY;
        }
    }
}
