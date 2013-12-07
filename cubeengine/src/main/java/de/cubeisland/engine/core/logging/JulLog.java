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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import de.cubeisland.engine.core.Core;

import static de.cubeisland.engine.core.logging.Level.DEBUG;
import static de.cubeisland.engine.core.logging.Level.TRACE;

public class JulLog extends Log<Logger>
{
    private String prefix;

    public JulLog(Logger julLogger, Core core, String name)
    {
        this.handle = julLogger;
        try
        {
            FileHandler fileHandler = new FileHandler(core.getFileManager().getLogPath().resolve(name + ".log").toString(), true);
            this.handle.addHandler(fileHandler);
            fileHandler.setFormatter(new FileFormater());
        }
        catch (IOException e)
        {}
    }

    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    @Override
    public void log(Level level, Throwable throwable, String message, Object... args)
    {
        message = FileFormater.parse(message, args);
        this.handle.log(level.getJulLevel(), message);
        if (throwable != null)
        {
            if (level == TRACE)
            {
                this.handle.log(TRACE.getJulLevel(), throwable.getLocalizedMessage(), throwable);
            }
            else
            {
                this.handle.log(DEBUG.getJulLevel(), throwable.getLocalizedMessage(), throwable);
            }
        }
        if (this.parentLog != null)
        {
            if (prefix != null)
            {
                message = prefix + message;
            }
            this.parentLog.log(level, throwable, message);
        }
    }

    @Override
    public void setLevel(Level level)
    {
        this.level = level;
        this.handle.setLevel(level.getJulLevel());
    }

    @Override
    public Level getLevel()
    {
        return this.level;
    }
}
