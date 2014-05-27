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

import java.io.File;
import java.text.SimpleDateFormat;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.logging.target.file.cycler.FilesizeCycler;
import de.cubeisland.engine.logging.target.file.format.FileFormat;
import de.cubeisland.engine.logging.target.file.format.LogFileFormat;

public class LoggingUtil
{
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static File getLogFile(Core core, String name)
    {
        return core.getFileManager().getLogPath().resolve(name + ".log").toFile();
    }

    public static FileFormat getFileFormat(boolean withDate, boolean withLevel)
    {
        if (withDate)
        {
            if (withLevel)
            {
                return new LogFileFormat("{date} [{level}] {msg}", sdf);
            }
            else
            {
                return new LogFileFormat("{date} {msg}", sdf);
            }
        }
        if (withLevel)
        {
            return new LogFileFormat("[{level}] {msg}", sdf);
        }
        else
        {
            return new LogFileFormat("{msg}", sdf);
        }
    }

    public static FilesizeCycler getCycler()
    {
        return new FilesizeCycler(20000000L, "{name}/{name}_{date}{_i}{ending}");
    }
}
