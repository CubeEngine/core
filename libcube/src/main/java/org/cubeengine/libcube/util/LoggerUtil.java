/*
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
package org.cubeengine.libcube.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.Message;

public class LoggerUtil
{
    private static Logger getLogger(String name)
    {
        if (name == null)
        {
            return (Logger) LogManager.getLogger();
        }
        return (Logger)LogManager.getLogger(name);
    }

    public static void setLoggerLevel(String name, String level)
    {
        getLogger(name).setLevel(Level.toLevel(level));
    }

    public static Object attachCallback(String name, LogCallback callback)
    {
        Appender app = new CallbackAppender(callback);
        app.start();
        getLogger(name).addAppender(app);
        return app;
    }

    public static void removeCallback(String name, Object handle)
    {
        if (!(handle instanceof Appender))
        {
            throw new IllegalArgumentException("The handle must be a log4j appender!");
        }
        Appender app = (Appender) handle;
        app.stop();
        getLogger(name).removeAppender(app);
    }

    private static class CallbackAppender extends AbstractAppender
    {

        private final LogCallback callback;

        public CallbackAppender(LogCallback callback)
        {
            super("cubeengine", null, PatternLayout.newBuilder().build());
            this.callback = callback;
        }

        @Override
        public void append(LogEvent logEvent)
        {
            Message msg = logEvent.getMessage();
            callback.invoke(msg.getFormat(), msg.getParameters(), msg.getThrowable(), msg.getFormattedMessage());
        }

        @Override
        public boolean isFiltered(LogEvent event)
        {
            return false;
        }
    }

    @FunctionalInterface
    public interface LogCallback
    {
        void invoke(String format, Object[] args, Throwable t, String formatted);
    }
}
