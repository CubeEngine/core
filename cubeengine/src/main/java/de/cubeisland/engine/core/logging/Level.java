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

import java.util.Locale;

public enum Level
{
    ALL   ("all", ch.qos.logback.classic.Level.ALL),
    TRACE ("trace", ch.qos.logback.classic.Level.TRACE),
    DEBUG ("debug", ch.qos.logback.classic.Level.DEBUG),
    INFO  ("info", ch.qos.logback.classic.Level.INFO),
    WARN  ("warn", ch.qos.logback.classic.Level.WARN),
    ERROR ("error", ch.qos.logback.classic.Level.ERROR),
    OFF   ("off", ch.qos.logback.classic.Level.OFF);

    private final String name;
    private final ch.qos.logback.classic.Level level;

    private Level(String name, ch.qos.logback.classic.Level level)
    {
        this.name = name;
        this.level = level;
    }

    public static Level toLevel(String level, Level fallback)
    {
        Level lvl = Level.valueOf(level.toUpperCase(Locale.ENGLISH));
        if (lvl == null)
        {
            lvl = fallback;
        }
        return lvl;
    }

    public ch.qos.logback.classic.Level getLevel()
    {
        return level;
    }
}
