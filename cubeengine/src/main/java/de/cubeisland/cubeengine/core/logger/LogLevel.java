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

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

/**
 * ALL > DEBUG > INFO > NOTICE > WARNING > ERROR > OFF
 */
public final class LogLevel
{
    static final ConcurrentMap<String, CubeLevel> LEVELS = new ConcurrentHashMap<String, CubeLevel>(7);

    public static final CubeLevel ALL = new CubeLevel(Level.ALL);
    public static final CubeLevel OFF = new CubeLevel(Level.OFF);

    public static final CubeLevel ERROR = new CubeLevel("Error", 10000);
    public static final CubeLevel WARNING = new CubeLevel("Warning", 9000);
    public static final CubeLevel NOTICE = new CubeLevel("Notice", 8000);
    public static final CubeLevel INFO = new CubeLevel("Info", 7000);
    public static final CubeLevel DEBUG = new CubeLevel("Debug", 6000);

    private LogLevel()
    {}

    public static CubeLevel parse(String name)
    {
        return LEVELS.get(name.toUpperCase(Locale.ENGLISH));
    }

}
