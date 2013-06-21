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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;

public class ColorConverter extends CompositeConverter<ILoggingEvent>
{
    @Override
    protected String transform(ILoggingEvent event, String in)
    {
        Ansi ansi = Ansi.ansi();
        String message = event.getMessage();
        String prefix = null;
        if (message.startsWith("[") && message.contains("] "))
        {
            message = message.substring(message.indexOf("] "));
            prefix = message.substring(0, message.indexOf("] "));
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
        if (prefix != null)
        {
            message = prefix + message;
        }
        return message;
    }
}
