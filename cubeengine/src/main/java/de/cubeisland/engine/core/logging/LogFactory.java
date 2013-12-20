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

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.logging.DefaultLogFactory;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogTarget;
import de.cubeisland.engine.logging.filter.ExceptionFilter;
import de.cubeisland.engine.logging.filter.PrefixFilter;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import de.cubeisland.engine.logging.target.proxy.LogProxyTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class LogFactory extends DefaultLogFactory
{
    protected Core core;
    protected LogTarget exceptionTarget;

    protected Log coreLog;
    private Log parent;

    public LogFactory(Core core, java.util.logging.Logger julLogger)
    {
        this.core = core;
        this.parent = this.getLog(core.getClass());
        Log4jProxyTarget log4jProxyTarget = new Log4jProxyTarget((Logger)LogManager.getLogger(julLogger.getName()));
        log4jProxyTarget.setProxyLevel(core.getConfiguration().logging.consoleLevel);
        this.parent.addTarget(log4jProxyTarget);

        Log exLog = this.getLog(Core.class, "Exceptions");
        exLog.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "Exceptions"),
                                               LoggingUtil.getFileFormat(true, false),
                                               true, LoggingUtil.getCycler(),
                                               core.getTaskManager().getThreadFactory()));
        this.exceptionTarget = new LogProxyTarget(exLog);
        this.exceptionTarget.appendFilter(new ExceptionFilter());
        this.parent.addTarget(exceptionTarget);
        log4jProxyTarget.appendFilter(new PrefixFilter("[CubeEngine] "));

        if (core.getConfiguration().logging.logCommands)
        {
            Log commands = this.getLog(Core.class, "Commands");
            commands.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "Commands"),
                                                   LoggingUtil.getFileFormat(true, false),
                                                   true, LoggingUtil.getCycler(),
                                                   core.getTaskManager().getThreadFactory()));
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
            this.coreLog = this.getLog(Core.class, "Core");
            AsyncFileTarget target = new AsyncFileTarget(LoggingUtil.getLogFile(core, "Core"),
                                                         LoggingUtil.getFileFormat(true, true),
                                                         true, LoggingUtil.getCycler(),
                                                         core.getTaskManager().getThreadFactory());
            target.setLevel(this.core.getConfiguration().logging.fileLevel);
            coreLog.addTarget(target);
            this.coreLog.addDelegate(this.getParent());
        }
        return this.coreLog;
    }

    public Log getParent()
    {
        return this.parent;
    }
}
