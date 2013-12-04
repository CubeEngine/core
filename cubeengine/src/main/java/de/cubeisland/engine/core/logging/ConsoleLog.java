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

import java.util.logging.Logger;

import org.apache.logging.log4j.LogManager;

import static de.cubeisland.engine.core.logging.Level.DEBUG;
import static de.cubeisland.engine.core.logging.Level.TRACE;

public class ConsoleLog extends Log<org.apache.logging.log4j.core.Logger>
{
    private final String prefix = "[CubeEngine] ";

    public ConsoleLog(Logger pluginLogger)
    {
        this.handle = (org.apache.logging.log4j.core.Logger)LogManager.getLogger(pluginLogger.getName());
    }

    @Override
    public void log(Level level, Throwable throwable, String message, Object... args)
    {
        message = FileFormater.parse(message, args);
        message = prefix + message;
        this.handle.log(level.getL4jLevel(), message);
        if (throwable != null)
        {
            if (level == TRACE)
            {
                this.handle.log(TRACE.getL4jLevel(), throwable.getLocalizedMessage(), throwable);
            }
            else
            {
                this.handle.log(DEBUG.getL4jLevel(), throwable.getLocalizedMessage(), throwable);
            }
        }
    }

    @Override
    public void setLevel(Level level)
    {
        this.handle.setLevel(level.getL4jLevel());
    }

    @Override
    public Level getLevel()
    {
        return null;
    }
}
