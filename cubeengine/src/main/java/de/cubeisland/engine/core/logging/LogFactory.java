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

import java.util.logging.Logger;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.ModuleInfo;

public class LogFactory
{
    private static final String BASE_NAME = "cubeengine";

    protected Core core;
    protected final Logger julLogger;

    protected ConsoleLog parentLogger; // console logging
    protected JulLog exceptionLogger; // TODO

    protected Log coreLog;

    public LogFactory(Core core, java.util.logging.Logger julLogger)
    {
        this.core = core;
        this.julLogger = julLogger;

        this.parentLogger = new ConsoleLog(julLogger);
        this.parentLogger.setLevel(core.getConfiguration().logging.consoleLevel);
        if (!core.getConfiguration().logging.logCommands)
        {
            //this.getLog("commands").getHandle().setAdditive(false); // TODO
        }
    }

    /**
     * Get or create the logging for the core
     *
     * @return The logging for the core
     */
    public synchronized Log getCoreLog()
    {
        if (this.coreLog == null)
        {
            this.coreLog = new JulLog(julLogger).setParent(parentLogger);
        }
        return this.coreLog;
    }

    /**
     * Get or create a logging for the module
     * @param info The module
     * @return The logging for the module
     */
    public Log createModuleLog(ModuleInfo info)
    {
        Logger logger = Logger.getLogger(BASE_NAME + "." + info.getId());
        logger.setUseParentHandlers(false); // ignore bukkit parentlogger
        //TODO add filehandler
        return new ModuleLogger(logger, info).setParent(parentLogger);
    }

    public Log getLog(String name)
    {
        Logger logger = Logger.getLogger(BASE_NAME + "." + name);
        logger.setUseParentHandlers(false); // ignore bukkit parentlogger
        //TODO add filehandler
        return new JulLog(logger);
    }

    //abstract void shutdown();

    //abstract void shutdown(Log log);
}
