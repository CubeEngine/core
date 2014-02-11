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
package de.cubeisland.engine.core.bukkit;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.message.Message;

public class CommandLogFilter implements Filter
{
    private final Pattern DETECTION_PATTERN = Pattern.compile("[\\w\\d\\-\\.]{3,16} issued server command: /.+", Pattern.CASE_INSENSITIVE);

    @Override
    public Result getOnMismatch()
    {
        return Result.DENY;
    }

    @Override
    public Result getOnMatch()
    {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, org.apache.logging.log4j.Level level, Marker marker, String s, Object... objects)
    {
        if (level == org.apache.logging.log4j.Level.INFO)
        {
            if (DETECTION_PATTERN.matcher(s).find())
            {
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, org.apache.logging.log4j.Level level, Marker marker, Object o, Throwable throwable)
    {
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(Logger logger, org.apache.logging.log4j.Level level, Marker marker, Message message, Throwable throwable)
    {
        if (level == org.apache.logging.log4j.Level.INFO)
        {
            if (DETECTION_PATTERN.matcher(message.getFormattedMessage()).find())
            {
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }

    @Override
    public Result filter(LogEvent logEvent)
    {
        if (logEvent.getLevel() == org.apache.logging.log4j.Level.INFO)
        {
            if (DETECTION_PATTERN.matcher(logEvent.getMessage().getFormattedMessage()).find())
            {
                return Result.DENY;
            }
        }
        return Result.NEUTRAL;
    }
}
