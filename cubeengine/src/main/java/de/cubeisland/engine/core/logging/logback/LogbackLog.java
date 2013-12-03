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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LogbackLog
{
    private final Logger logger;

    public LogbackLog(Logger logger)
    {
        this.logger = logger;
    }


    public void log(Level level, Throwable throwable, String message, Object... args)
    {
        if (level == Level.TRACE)
        {
            this.logger.trace(message, args);
            if (throwable != null)
            {
                this.logger.trace(throwable.getLocalizedMessage(), throwable);
            }
            return;
        }
        else if (level == Level.DEBUG)
        {
            this.logger.debug(message, args);
        }
        else if (level == Level.INFO)
        {
            this.logger.info(message, args);
        }
        else if (level == Level.WARN)
        {
            this.logger.warn(message, args);
        }
        else if (level == Level.ERROR)
        {
            this.logger.error(message, args);
        }
        if (throwable != null)
        {
            this.logger.debug(throwable.getLocalizedMessage(), throwable);
        }
    }

    public de.cubeisland.engine.core.logging.Level getLevel()
    {
        return de.cubeisland.engine.core.logging.Level.toLevel(this.logger.getLevel().toString());
    }
}
