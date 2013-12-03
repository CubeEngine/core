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
    ALL(java.util.logging.Level.ALL, org.apache.logging.log4j.Level.ALL),
    TRACE(new CELevel("TRACE", java.util.logging.Level.INFO.intValue() + 33), org.apache.logging.log4j.Level.TRACE),
    DEBUG(new CELevel("DEBUG", java.util.logging.Level.INFO.intValue() + 66), org.apache.logging.log4j.Level.DEBUG),
    INFO(new CELevel("INFO", java.util.logging.Level.INFO.intValue()), org.apache.logging.log4j.Level.INFO),
    WARN(new CELevel("WARN", java.util.logging.Level.WARNING.intValue()), org.apache.logging.log4j.Level.WARN),
    ERROR(new CELevel("ERROR", java.util.logging.Level.SEVERE.intValue()), org.apache.logging.log4j.Level.ERROR),
    OFF(java.util.logging.Level.OFF, org.apache.logging.log4j.Level.OFF);

    private java.util.logging.Level jul;
    private org.apache.logging.log4j.Level l4j;

    private Level(java.util.logging.Level jul, org.apache.logging.log4j.Level l4j)
    {
        this.jul = jul;
        this.l4j = l4j;
    }

    public static Level toLevel(String level)
    {
        return Level.valueOf(level.toUpperCase(Locale.ENGLISH));
    }

    public java.util.logging.Level getJulLevel()
    {
        return this.jul;
    }

    public org.apache.logging.log4j.Level getL4jLevel()
    {
        return this.l4j;
    }

    public static class CELevel extends java.util.logging.Level
    {
        public CELevel(String name, int level)
        {
            super(name, level);
        }
    }
}
