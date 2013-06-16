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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.ErrorStatus;

public class BukkitAppender extends AppenderBase<ILoggingEvent>
{
    private Logger logger;
    public Layout<ILoggingEvent> layout;

    public void start()
    {
        if (this.layout == null) {
            addStatus(new ErrorStatus("No layout set for the appender named \""  + name + "\".", this));
        }
        else
        {
            this.logger = Bukkit.getPluginManager().getPlugin("CubeEngine").getLogger();
            super.start();
        }
    }

    @Override
    protected void append(ILoggingEvent event)
    {
        Level level = null;
        if (event.getLevel().toString().equalsIgnoreCase("error"))
        {
            level = Level.SEVERE;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("warn"))
        {
            level = Level.WARNING;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("debug"))
        {
            level = Level.FINE;
        }
        else if (event.getLevel().toString().equalsIgnoreCase("trace"))
        {
            level = Level.FINEST;
        }
        else
        {
            level = Level.INFO;
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
}