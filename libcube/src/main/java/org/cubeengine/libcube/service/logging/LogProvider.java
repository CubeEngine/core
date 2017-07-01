/*
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
package org.cubeengine.libcube.service.logging;

import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.LogTarget;
import de.cubeisland.engine.logscribe.filter.PrefixFilter;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.filesystem.FileManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class LogProvider
{
    private LogFactory logFactory;
    private FileManager fm;
    private ModuleManager mm;
    private Map<String, Log> loggers = new HashMap<>();

    @Inject
    public LogProvider(LogFactory logFactory, FileManager fm, ModuleManager mm)
    {
        this.logFactory = logFactory;
        this.fm = fm;
        this.mm = mm;
    }

    public Log getLogger(Class owner, String name, boolean module)
    {
        String longName = owner.getName() + "#" + name;
        Log logger = loggers.get(longName);
        if (logger != null)
        {
            return logger;
        }
        Log baseLogger = logFactory.getLog(LogFactory.class, "CubeEngine");
        if (module)
        {
            logger = logFactory.getLog(owner, name);
            logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(fm, name),
                    LoggingUtil.getFileFormat(true, true), true, LoggingUtil.getCycler(), mm.getThreadFactory(owner)));

            LogTarget parentTarget = logger.addDelegate(baseLogger); // delegate to main logger
            parentTarget.appendFilter(new PrefixFilter("[" + name + "] "));
        }
        else
        {
            logger = logFactory.getLog(owner, name);
            LogTarget parentTarget = logger.addDelegate(baseLogger); // delegate to main logger

            parentTarget.appendFilter(new PrefixFilter("[" + name + "] "));
            // TODO manually add Target for non-modules
        }
        this.loggers.put(longName, logger);
        return logger;
    }
}
