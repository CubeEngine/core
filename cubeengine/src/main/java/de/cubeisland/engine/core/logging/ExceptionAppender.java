/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.logging;

import java.util.Arrays;

import de.cubeisland.engine.logging.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;

public class ExceptionAppender extends AbstractAppender
{
    private Log exLog;

    private StackTraceElement[] lastException = null;
    private int count = 0;

    public ExceptionAppender(Log exLog)
    {
        super("ExceptionAppender", new ExceptionFilter(), PatternLayout.createLayout(null, null, null, null, null));
        this.exLog = exLog;
    }

    @Override
    public void append(LogEvent logEvent)
    {
        Throwable thrown = logEvent.getThrown();
        if (Arrays.equals(thrown.getStackTrace(), lastException))
        {
            exLog.error(logEvent.getMessage().getFormat() + " x" + ++count, logEvent.getMessage().getParameters());
        }
        else
        {
            count = 1;
            this.lastException = thrown.getStackTrace();
            exLog.error(thrown, logEvent.getMessage().getFormat(),  logEvent.getMessage().getParameters());
        }
    }

    private static class ExceptionFilter implements Filter
    {
        @Override
        public Result getOnMismatch()
        {
            return Result.DENY;
        }

        @Override
        public Result getOnMatch()
        {
            return Result.ACCEPT;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, String s, Object... objects)
        {
            return Result.DENY;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Object o, Throwable throwable)
        {
            if (throwable != null)
            {
                return Result.ACCEPT;
            }
            return Result.DENY;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker, Message message, Throwable throwable)
        {
            if (throwable != null)
            {
                return Result.ACCEPT;
            }
            return Result.DENY;
        }

        @Override
        public Result filter(LogEvent logEvent)
        {
            if (logEvent.getThrown() != null)
            {
                return Result.ACCEPT;
            }
            return Result.DENY;
        }
    }
}
