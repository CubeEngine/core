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
package de.cubeisland.cubeengine.core.module;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;

import java.io.File;
import java.util.Locale;
import java.util.logging.LogRecord;

/**
 * This logger is used to log module messages.
 */
public class ModuleLogger extends CubeLogger
{
    private final String prefix;

    public ModuleLogger(Core core, ModuleInfo info)
    {
        super(info.getName(), core.getLog());
        this.prefix = "[" + info.getName() + "] ";
        try
        {
            this.addHandler(new CubeFileHandler(LogLevel.ALL, new File(core.getFileManager().getLogDir(), info.getName().toLowerCase(Locale.ENGLISH)).toString()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(LogRecord record)
    {
        record.setMessage(this.prefix + record.getMessage());
        super.log(record);
    }
}
