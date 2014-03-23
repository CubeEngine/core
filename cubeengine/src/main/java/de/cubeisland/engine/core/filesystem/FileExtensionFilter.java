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
package de.cubeisland.engine.core.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class can be used to creates file or filename filters.
 */
public class FileExtensionFilter implements Filter<Path>, FileFilter, FilenameFilter
{
    public static final FileExtensionFilter TXT = new FileExtensionFilter("txt");
    public static final FileExtensionFilter YAML = new FileExtensionFilter("yml");
    public static final FileExtensionFilter JSON = new FileExtensionFilter("json");
    public static final FileExtensionFilter INI = new FileExtensionFilter("ini");
    public static final FileExtensionFilter JAR = new FileExtensionFilter("jar");
    public static final FileExtensionFilter LOG = new FileExtensionFilter("log");
    public static final FileExtensionFilter PO = new FileExtensionFilter("po");
    private final String extention;

    public FileExtensionFilter(String extention)
    {
        if (!extention.startsWith("."))
        {
            extention = "." + extention;
        }
        this.extention = extention;
    }

    @Override
    public boolean accept(Path entry) throws IOException
    {
        return Files.isRegularFile(entry) && entry.toString().endsWith(this.extention); // Path.endsWith() does not only check the extension but the whole fileName
    }

    @Override
    public boolean accept(File file)
    {
        return (file.isFile() && file.toString().endsWith(this.extention));
    }

    @Override
    public boolean accept(File dir, String name)
    {
        return name.endsWith(this.extention);
    }

    /**
     * Returns the extention this filter uses
     *
     * @return the extention
     */
    public String getExtention()
    {
        return this.extention;
    }
}
