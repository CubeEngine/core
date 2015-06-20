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
package de.cubeisland.engine.service.logging;

import javax.inject.Inject;
import de.cubeisland.engine.logscribe.DefaultLogFactory;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.filter.PrefixFilter;
import de.cubeisland.engine.modularity.asm.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@ServiceProvider(LogFactory.class)
public class SpongeLogFactory extends DefaultLogFactory
{
    private final Log4jProxyTarget baseTarget;

    @Inject
    public SpongeLogFactory()
    {
        baseTarget = new Log4jProxyTarget((Logger)LogManager.getLogger("CubeEngine"));
        baseTarget.appendFilter(new PrefixFilter("[CubeEngine] "));

        getLog(CoreModule.class).addTarget(baseTarget);
    }

    @Enable
    public void onEnable()
    {
        // Start Logging Exceptions
        Log exLog = this.getLog(CoreModule.class, "Exceptions");
        ExceptionAppender exceptionAppender = new ExceptionAppender(exLog);
        exceptionAppender.start();
        ((Logger)LogManager.getLogger("Minecraft")).addAppender(exceptionAppender);
        baseTarget.getHandle().addAppender(exceptionAppender);
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
