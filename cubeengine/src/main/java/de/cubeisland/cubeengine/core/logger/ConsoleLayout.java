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
package de.cubeisland.cubeengine.core.logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

public class ConsoleLayout extends PatternLayout
{
    public String doLayout(ILoggingEvent event) {
        LoggingEvent proxy = new LoggingEvent();
        proxy.setArgumentArray(event.getArgumentArray());
        proxy.setCallerData(event.getCallerData());
        proxy.setLevel(event.getLevel());
        proxy.setLoggerContextRemoteView(event.getLoggerContextVO());
        proxy.setLoggerName(event.getLoggerName());
        proxy.setMarker(event.getMarker());
        proxy.setMDCPropertyMap(event.getMDCPropertyMap());
        proxy.setThreadName(event.getThreadName());
        proxy.setThrowableProxy((ThrowableProxy)event.getThrowableProxy());
        proxy.setTimeStamp(event.getTimeStamp());

        Ansi ansi = Ansi.ansi();
        String message = event.getMessage();
        if (message.startsWith("[") && message.contains("] "))
        {
            message = message.substring(message.indexOf("] "));
        }
        if (event.getLevel().equals(Level.ERROR))
        {
            message = ansi.bold().fgBright(Color.RED).a(message).reset().toString();
        }
        else if (event.getLevel().equals(Level.WARN))
        {
            message = ansi.bold().fgBright(Color.YELLOW).a(message).reset().toString();
        }
        else if (event.getLevel().equals(Level.DEBUG))
        {
            message = ansi.fg(Color.WHITE).a(message).reset().toString();
        }
        else if (event.getLevel().equals(Level.TRACE))
        {
            message = ansi.fgBright(Color.BLACK).a(message).reset().toString();
        }
        proxy.setMessage(message);
        return super.doLayout(proxy);
    }
}
