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
package de.cubeisland.engine.core.bukkit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.filesystem.FileUtil;

import static de.cubeisland.engine.core.filesystem.FileExtensionFilter.LOG;
import static java.util.logging.Level.WARNING;

public class BukkitFileManager extends FileManager
{
    private final BukkitCore core;

    public BukkitFileManager(BukkitCore core) throws IOException
    {
        super(core.getLogger(), core.getDataFolder().toPath());
        this.core = core;
    }

    private static final SimpleDateFormat LOG_DIR_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd--HHmm");

    public void cycleLogs()
    {
        if (this.core.getConfiguration().logging.archiveLogs)
        {
            String dateString = LOG_DIR_DATE_FORMAT.format(new Date(core.getLog().getLoggerContext().getBirthTime()));
            final Path base = Paths.get(System.getProperty("cubeengine.logger.default-path"));
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
        }
    }
}
