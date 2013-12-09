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
import java.util.logging.Logger;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogTarget;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import de.cubeisland.engine.logging.target.file.cycler.LogCycler;
import de.cubeisland.engine.logging.target.file.format.LogFileFormat;

public class TestLogFactory extends LogFactory
{
    public TestLogFactory(Core core, Logger julLogger)
    {
        super(core, julLogger);
    }

    @Override
    protected LogTarget addFileTarget(Log log, File file, String formatString)
    {
        LogFileFormat fileFormat = new LogFileFormat(formatString, sdf);
        LogCycler cycler = null;// TODO cycler
        AsyncFileTarget target = new AsyncFileTarget(file, fileFormat, true, cycler, null);
        log.addTarget(target);
        return target;
    }
}
