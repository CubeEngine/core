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

import static org.cubeengine.libcube.service.logging.LoggingUtil.getCycler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.AnnotationCommandBuilder;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.logscribe.DefaultLogFactory;
import org.cubeengine.logscribe.Log;
import org.cubeengine.logscribe.LogFactory;
import org.cubeengine.logscribe.target.file.AsyncFileTarget;
import org.cubeengine.reflect.Reflector;

import java.util.concurrent.ThreadFactory;

public class SpongeLogFactory extends DefaultLogFactory
{
    private final Log4jProxyTarget baseTarget;
    private final Log mainLogger;
    private final LoggerConfiguration config;
    private final FileManager fm;
    private ThreadFactory tf;
    private ModuleManager mm;

    public SpongeLogFactory(Reflector reflector, FileManager fm, ModuleManager mm)
    {
        this.fm = fm;
        this.mm = mm;

        config = reflector.load(LoggerConfiguration.class, mm.getBasePath().toPath().resolve("logger.yml").toFile());

        // configure console logger
        baseTarget = new Log4jProxyTarget((Logger)LogManager.getLogger("CubeEngine"));
        // Sponge is already adding this baseTarget.appendFilter(new PrefixFilter("[CubeEngine] "));
        baseTarget.setLevel((config.consoleLevel));

        // create main logger and attach console logger
        mainLogger = getLog(LogFactory.class, "CubeEngine").addTarget(baseTarget);
    }

    public void init(ThreadFactory tf)
    {
        this.tf = tf;
        // configure main file logger
        AsyncFileTarget mainFileTarget =
                new AsyncFileTarget.Builder(LoggingUtil.getLogFile(fm, "main").toPath(),
                        LoggingUtil.getFileFormat(true, true)
                ).setAppend(true).setCycler(getCycler()).setThreadFactory(tf).build();
        mainFileTarget.setLevel(config.fileLevel);
        mainLogger.addTarget(mainFileTarget);

        // configure exception logger
        Log exLog = getLog(LogFactory.class, "Exceptions");
        exLog.addTarget(
                new AsyncFileTarget.Builder(LoggingUtil.getLogFile(fm, "Exceptions").toPath(),
                        LoggingUtil.getFileFormat(true, false)
                ).setAppend(true).setCycler(getCycler()).setThreadFactory(tf).build());

        // hook into Minecraft Console Logger
        ExceptionAppender exceptionAppender = new ExceptionAppender(exLog);
        exceptionAppender.start();
        ((Logger)LogManager.getLogger("Minecraft")).addAppender(exceptionAppender);
        baseTarget.getHandle().addAppender(exceptionAppender);

        if (!config.logCommands)
        {
            CommandLogging.disable();
        }
    }

    public void onDisable()
    {
        CommandLogging.reset();
    }

    @Override
    public Log getLog(Class<?> clazz, String id)
    {
        if (config.logCommands && clazz.equals(AnnotationCommandBuilder.class)) // TODO command logging
        {
            Log cmdLogger = super.getLog(clazz, id);
            cmdLogger.addTarget(
                    new AsyncFileTarget.Builder(LoggingUtil.getLogFile(fm, "commands").toPath(),
                            LoggingUtil.getFileFormat(true, false)
                    ).setAppend(true).setCycler(getCycler()).setThreadFactory(tf).build());
            return cmdLogger;
        }
        return super.getLog(clazz, id);
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
