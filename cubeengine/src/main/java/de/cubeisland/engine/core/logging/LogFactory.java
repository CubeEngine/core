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
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.logging.DefaultLogFactory;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogTarget;
import de.cubeisland.engine.logging.filter.ExceptionFilter;
import de.cubeisland.engine.logging.filter.PrefixFilter;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import de.cubeisland.engine.logging.target.file.cycler.LogCycler;
import de.cubeisland.engine.logging.target.file.format.LogFileFormat;
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

        this.exceptionTarget = new LogProxyTarget(this.createFileLog(Core.class, "Exceptions"));
        this.exceptionTarget.appendFilter(new ExceptionFilter());
        this.parent.addTarget(exceptionTarget);
        log4jProxyTarget.appendFilter(new PrefixFilter("[CubeEngine] "));

        if (core.getConfiguration().logging.logCommands)
        {
            Log commands = this.getLog(Core.class, "commands");
            this.addFileTarget(commands, this.getLogFile("Commands"), "{date} {msg}", true);
        }
    }

    /**
     * Get or create the logging for the core
     *
     * @return The logging for the core
     */
    public synchronized Log createCoreLog()
    {
        if (this.coreLog == null)
        {
            this.coreLog = this.getLog(Core.class);
            LogTarget target = this.addFileTarget(this.coreLog, this.getLogFile("Core"), "{date} [{level}] {msg}", true);
            target.setLevel(this.core.getConfiguration().logging.fileLevel);
            this.coreLog.addTarget(new LogProxyTarget(this.parent));
        }
        return this.coreLog;
    }

    protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Log createModuleLog(Module module)
    {
        Log log = this.getLog(module.getClass());
        File file = this.getLogFile(module.getName());
        this.addFileTarget(log, file, "{date} [{level}] {msg}", true);
        LogTarget proxy = log.addDelegate(parent);
        proxy.appendFilter(new PrefixFilter("[" + module.getName() + "] "));
        return log;
    }

    private File getLogFile(String name)
    {
        return this.core.getFileManager().getLogPath().resolve(name + ".log").toFile();
    }

    protected LogTarget addFileTarget(Log log, File file, String formatString, boolean append)
    {
        LogFileFormat fileFormat = new LogFileFormat(formatString, sdf);
        LogCycler cycler = null;// TODO cycler
        AsyncFileTarget target = new AsyncFileTarget(file, fileFormat, append, cycler, core.getTaskManager().getThreadFactory());
        log.addTarget(target);
        return target;
    }

    public Log createFileLog(Class clazz, String name)
    {
        return this.createFileLog(clazz, name, true, true);
    }

    public Log createFileLog(Class clazz, String name, boolean withLevel, boolean append)
    {
        Log log = this.getLog(clazz, name);
        File file = this.getLogFile(name);
        this.addFileTarget(log, file, "{date}" + (withLevel ? " [{level}]" : " ") + "{msg}", append);
        return log;
    }

    public Log createFileLog(Class clazz, String name, boolean withLevel, boolean withDate, boolean append)
    {
        if (withDate)
        {
            return this.createFileLog(clazz, name, withLevel, append);
        }
        Log log = this.getLog(clazz, name);
        File file = this.getLogFile(name);
        this.addFileTarget(log, file, (withLevel ? "[{level}] " : "") + "{msg}", append);
        return log;
    }
}
