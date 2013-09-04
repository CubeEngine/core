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
package de.cubeisland.engine.core.logging.logback;

import java.util.logging.Level;
import java.util.logging.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.ErrorStatus;

/**
 * An appender for LogBack that forwards all output to a java.util.logging.Log
 * It does support a layout.
 */
public class JULAppender  extends AppenderBase<ILoggingEvent>
{

    private Logger logger;
    private Layout<ILoggingEvent> layout;

    public void start()
    {
        if (this.layout == null) {
            addStatus(new ErrorStatus("No layout set for the appender named \""  + name + "\".", this));
        }
        if (this.logger == null)
        {
            addStatus(new ErrorStatus("No logging set for the appender named \"" + name + "\"", this));
        }
        else
        {
            super.start();
        }
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        if (!this.isStarted())
        {
            return;
        }
        Level level;
        if (event.getLevel().equals(ch.qos.logback.classic.Level.ERROR))
        {
            level = LogBackLevel.error;
        }
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.WARN))
        {
            level = LogBackLevel.warn;
        }
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.DEBUG))
        {
            level = LogBackLevel.debug;
        }
        else if (event.getLevel().equals(ch.qos.logback.classic.Level.TRACE))
        {
            level = LogBackLevel.trace;
        }
        else
        {
            level = LogBackLevel.info;
        }

        // The PatternFormat have to have a newline at the end to give a newline to the exceptions, but we don't want an
        // extra newline if the exception isn't there.
        String message = layout.doLayout(event);
        if (message.endsWith("\n"))
        {
            message = message.substring(0,message.length()-1);
        }
        this.logger.log(level, message, event.getArgumentArray());
    }

    public void setLayout(Layout<ILoggingEvent> layout)
    {
        this.layout = layout;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    private static class LogBackLevel extends Level
    {

        public static final LogBackLevel trace = new LogBackLevel("TRACE", Level.INFO.intValue() + 33);
        public static final LogBackLevel debug = new LogBackLevel("DEBUG", Level.INFO.intValue() + 66);
        public static final LogBackLevel info = new LogBackLevel("INFO", Level.INFO.intValue());
        public static final LogBackLevel warn = new LogBackLevel("WARN", Level.WARNING.intValue());
        public static final LogBackLevel error = new LogBackLevel("ERROR", Level.SEVERE.intValue());

        public LogBackLevel(String name, int level)
        {
            super(name, level);
        }

    }
}
