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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.cubeisland.engine.core.filesystem.FileExtentionFilter;
import de.cubeisland.engine.core.filesystem.FileManager;

import static java.util.logging.Level.WARNING;

public class BukkitFileManager extends FileManager
{
    private final BukkitCore core;

    public BukkitFileManager(BukkitCore core) throws IOException
    {
        super(core, core.getDataFolder().getAbsoluteFile().getCanonicalFile());
        this.core = core;
    }

    private static final SimpleDateFormat LOG_DIR_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd--HHmm");

    public void cycleLogs()
    {
        if (this.core.getConfiguration().loggingArchiveLogs)
        {
            String dateString = LOG_DIR_DATE_FORMAT.format(new Date(core.getLog().getLoggerContext().getBirthTime()));
            final String folderPath = System.getProperty("cubeengine.logger.default-path") + File.separator + dateString;
            final String zipPath = folderPath + ".zip";

            ZipOutputStream zip = null;
            try
            {
                zip = new ZipOutputStream(new FileOutputStream(zipPath));
                File logFolder = new File(folderPath);

                if (!logFolder.exists() || !logFolder.isDirectory())
                {
                    zip.close();
                    this.core.getLogger().info("The old log directory was not found or is not a directory: " + folderPath);
                    return;
                }
                File[] files = logFolder.listFiles((FilenameFilter)FileExtentionFilter.LOG);
                if (files == null)
                {
                    this.core.getLogger().info("Failed to get the files of the log directory: " + folderPath);
                    return;
                }
                for (File file : files)
                {
                    if (!file.isFile())
                    {
                        continue;
                    }
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zip.putNextEntry(zipEntry);
                    FileInputStream logStream = new FileInputStream(file.getCanonicalFile());

                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = logStream.read(buffer)) != -1)
                    {
                        zip.write(buffer, 0, bytesRead);
                    }

                    zip.closeEntry();
                    logStream.close();
                }
                zip.finish();
                FileManager.deleteRecursive(logFolder);
            }
            catch (IOException ex)
            {
                core.getLogger().log(WARNING, "An error occurred while compressing the logs: " + ex
                    .getLocalizedMessage(), ex);
            }
            finally
            {
                if (zip != null)
                {
                    try
                    {
                        zip.close();
                    }
                    catch (IOException ignored)
                    {}
                }
            }
        }
    }
}
