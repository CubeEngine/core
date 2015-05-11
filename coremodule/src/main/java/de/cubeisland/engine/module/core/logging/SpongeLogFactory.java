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
package de.cubeisland.engine.module.core.logging;

import java.util.concurrent.ThreadFactory;
import de.cubeisland.engine.modularity.core.Module;

import de.cubeisland.engine.logscribe.DefaultLogFactory;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.filter.PrefixFilter;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import static de.cubeisland.engine.module.core.logging.LoggingUtil.getCycler;
import static de.cubeisland.engine.module.core.logging.LoggingUtil.getFileFormat;
import static de.cubeisland.engine.module.core.logging.LoggingUtil.getLogFile;

public class SpongeLogFactory extends DefaultLogFactory
{
    protected final CoreModule core;
    private final Log4jProxyTarget log4jProxyTarget;
    private Logger baseLogger;
    private Log exLog;
    private final Log parent;

    public SpongeLogFactory(CoreModule core, Logger baseLogger)
    {
        this.core = core;
        this.baseLogger = baseLogger;
        this.parent = this.getLog(core.getClass());


        log4jProxyTarget = new Log4jProxyTarget(baseLogger);
        this.parent.addTarget(log4jProxyTarget);

        log4jProxyTarget.appendFilter(new PrefixFilter("[CubeEngine] "));

        log4jProxyTarget.setProxyLevel(core.getConfiguration().logging.consoleLevel);
    }

    public void startExceptionLogger()
    {
        ThreadFactory threadFactory = core.getProvided(ThreadFactory.class);
        FileManager fileManager = core.getModularity().start(FileManager.class);
        exLog = this.getLog(CoreModule.class, "Exceptions");
        exLog.addTarget(new AsyncFileTarget(getLogFile(fileManager, "Exceptions"),
                                            getFileFormat(true, false),
                                            true, getCycler(), threadFactory));

        ExceptionAppender exceptionAppender = new ExceptionAppender(this.exLog);
        exceptionAppender.start();
        ((Logger)LogManager.getLogger("Minecraft")).addAppender(exceptionAppender);
        log4jProxyTarget.getHandle().addAppender(exceptionAppender);
    }


    public Log getParent()
    {
        return this.parent;
    }

    public void shutdown(Module module)
    {
        // TODO
        Log log = null;
        this.remove(log);
        log.shutdown();

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
