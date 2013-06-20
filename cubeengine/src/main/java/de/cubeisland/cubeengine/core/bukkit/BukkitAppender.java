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
package de.cubeisland.cubeengine.core.bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.logger.ConsoleLayout;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.ErrorStatus;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.fusesource.jansi.Ansi.Color;

public class BukkitAppender extends AppenderBase<ILoggingEvent>
{
    private Logger logger;
    public Layout<ILoggingEvent> layout;
    private final Level trace = new CustomLevel("TRACE", Level.INFO.intValue()+33);
    private final Level debug = new CustomLevel("DEBUG", Level.INFO.intValue()+66);
    private final Level info = new CustomLevel("INFO", Level.INFO.intValue());
    private final Level warn = new CustomLevel("WARN", Level.WARNING.intValue());
    private final Level error = new CustomLevel("ERROR", Level.SEVERE.intValue());

    public void start()
    {
        if (this.layout == null) {
            addStatus(new ErrorStatus("No layout set for the appender named \""  + name + "\".", this));
        }
        else
        {
            this.logger = ((BukkitCore)CubeEngine.getCore()).getLogger();
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
        if (event.getLevel().toString().equalsIgnoreCase("error"))
        {
            level = error;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("warn"))
        {
            level = warn;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("debug"))
        {
            level = debug;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("trace"))
        {
            level = trace;
        }
        else
        {
            level = info;
        }
        this.logger.log(level, layout.doLayout(event));
    }

    public void setLayout(Layout<ILoggingEvent> layout)
    {
        this.layout = layout;
    }

    public Layout<ILoggingEvent> getLayout()
    {
        return this.layout;
    }

    private class CustomLevel extends Level
    {
        CustomLevel(String name, int value)
        {
            super(name, value);
        }
    }
}
