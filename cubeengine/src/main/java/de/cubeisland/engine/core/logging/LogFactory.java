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
import de.cubeisland.engine.logging.LogLevel;
import de.cubeisland.engine.logging.filter.PrefixFilter;
import de.cubeisland.engine.logging.target.file.AsyncFileTarget;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class LogFactory extends DefaultLogFactory
{
    protected final Core core;

    private final Log exLog;

    protected Log coreLog;
    private final Log parent;
    private Log databaseLog;

    public LogFactory(Core core, java.util.logging.Logger julLogger)
    {
        this.core = core;
        this.parent = this.getLog(core.getClass());
        Log4jProxyTarget log4jProxyTarget = new Log4jProxyTarget((Logger)LogManager.getLogger(julLogger.getName()));
        this.parent.addTarget(log4jProxyTarget);

        exLog = this.getLog(Core.class, "Exceptions");
        exLog.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core, "Exceptions"),
                                            LoggingUtil.getFileFormat(true, false),
                                            true, LoggingUtil.getCycler(),
                                            core.getTaskManager().getThreadFactory()));

        ExceptionAppender exceptionAppender = new ExceptionAppender(this.exLog);
        exceptionAppender.start();
        ((Logger)LogManager.getLogger("Minecraft")).addAppender(exceptionAppender); // TODO add filter to log only our stuff?
        log4jProxyTarget.getHandle().addAppender(exceptionAppender);
        log4jProxyTarget.appendFilter(new PrefixFilter("[CubeEngine] "));

        log4jProxyTarget.setProxyLevel(core.getConfiguration().logging.consoleLevel);

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

    public Log getDatabaseLog()
    {
        if (this.databaseLog == null)
        {
            this.databaseLog = this.getLog(Core.class, "Database");
            AsyncFileTarget target = new AsyncFileTarget(LoggingUtil.getLogFile(core, "Database"),
                                                         LoggingUtil.getFileFormat(true, false),
                                                         true, LoggingUtil.getCycler(), core.getTaskManager().getThreadFactory());
            target.setLevel(this.core.getConfiguration().logging.logDatabaseQueries ? LogLevel.ALL : LogLevel.NONE);
            databaseLog.addTarget(target);
        }
        return this.databaseLog;
    }

    // TODO log-cycling on shutdown ?
    // old code:
    /*
    private static final SimpleDateFormat LOG_DIR_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd--HHmm");
    public void cycleLogs()
    {
        if (this.core.getConfiguration().logging.archiveLogs)
        {
            String dateString = LOG_DIR_DATE_FORMAT.format(new Date(core.getLogFactory().getBirthTime()));
            final Path base = Paths.get(System.getProperty("cubeengine.logging.default-path"));
            final Path folderPath = base.resolve(dateString);
            final Path zipPath = base.resolve(dateString + ".zip");

            try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(zipPath)))
            {
                if (!Files.exists(folderPath) || !Files.isDirectory(folderPath))
                {
                    this.core.getLogger().info("The old log directory was not found or is not a directory: " + folderPath);
                    return;
                }
                try (DirectoryStream<Path> directory = Files.newDirectoryStream(folderPath, LOG))
                {
                    for (Path file : directory)
                    {
                        ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
                        zip.putNextEntry(zipEntry);

                        try (FileChannel inputChannel = FileChannel.open(file))
                        {
                            ByteBuffer buffer = ByteBuffer.allocate(4096);
                            while (inputChannel.read(buffer) != -1)
                            {
                                zip.write(buffer.array(), 0, buffer.position());
                                buffer.flip();
                            }
                            zip.closeEntry();
                        }
                    }
                }
                zip.finish();
                FileUtil.deleteRecursive(folderPath);
            }
            catch (IOException ex)
            {
                core.getLogger().log(WARNING, "An error occurred while compressing the logs: " + ex
                    .getLocalizedMessage(), ex);
            }
        }*/
}
