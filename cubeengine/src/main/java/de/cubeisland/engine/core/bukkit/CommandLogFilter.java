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

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

public class CommandLogFilter implements Filter
{
    private final Pattern DETECTION_PATTERN = Pattern.compile("[\\w\\d\\-\\.]{3,16} issued server command: /.+", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isLoggable(LogRecord record)
    {
        if (record.getLevel() == Level.INFO)
        {
            if (DETECTION_PATTERN.matcher(record.getMessage()).find())
            {
                return false;
            }
        }
        return true;
    }
}
