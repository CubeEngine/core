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

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.logging.LogEntry;
import de.cubeisland.engine.logging.LogLevel;
import de.cubeisland.engine.logging.target.format.DefaultFormat;
import de.cubeisland.engine.logging.target.proxy.ProxyTarget;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;

public class Log4jProxyTarget extends ProxyTarget<Logger>
{
    private static final Map<LogLevel, Level> levelMap = new HashMap<>();

    static
    {
        levelMap.put(LogLevel.NONE, Level.OFF);
        //levelMap.put(LogLevel.FATAL, Level.FATAL);
        levelMap.put(LogLevel.ERROR, Level.ERROR);
        levelMap.put(LogLevel.WARN, Level.WARN);
        levelMap.put(LogLevel.INFO, Level.INFO);
        levelMap.put(LogLevel.DEBUG, Level.DEBUG);
        levelMap.put(LogLevel.TRACE, Level.TRACE);
        levelMap.put(LogLevel.ALL, Level.ALL);
    }

    public Log4jProxyTarget(Logger handle)
    {
        super(handle);
    }

    @Override
    protected void publish(LogEntry entry)
    {
        Level level = levelMap.get(entry.getLevel());
        if (level == null)
        {
            level = Level.INFO;
        }
        this.handle.log(level, DefaultFormat.parseArgs(entry.getMessage(), entry.getArgs()), entry.getThrowable());
    }

    @Override
    public void setProxyLevel(LogLevel level)
    {
        Level l4jLevel = levelMap.get(level);
        if (l4jLevel == null)
        {
            l4jLevel = Level.INFO;
        }
        this.handle.setLevel(l4jLevel);
    }

    @Override
    protected void shutdown0()
    {
        // this is not our logger! Let bukkit handle it
    }
}
