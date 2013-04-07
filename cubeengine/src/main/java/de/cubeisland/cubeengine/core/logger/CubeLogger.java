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

import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.ALL;

/**
 * This logger is used for all of CubeEngine's messages.
 */
public class CubeLogger extends Logger
{
    /**
     * Creates a new Logger by this name
     *
     * @param name the name
     */
    public CubeLogger(String name)
    {
        this(name, null);
    }

    /**
     * Creates a new Logger by this name
     *
     * @param name            the name
     * @param parent the parent logger
     */
    public CubeLogger(String name, Logger parent)
    {
        super(name, null);
        this.setLevel(ALL);
        if (parent != null)
        {
            this.setParent(parent);
        }
    }

    @Override
    public void log(LogRecord record)
    {
        if (this.getLevel().intValue() > LogLevel.DEBUG.intValue())
        {
            record.setThrown(null);
        }
        super.log(record);
    }
}
