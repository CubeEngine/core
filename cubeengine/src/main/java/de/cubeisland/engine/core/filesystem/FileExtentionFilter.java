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

/**
 * This class can be used to creates file or filename filters.
 */
public class FileExtentionFilter implements FileFilter, FilenameFilter
{
    public static final FileExtentionFilter TXT = new FileExtentionFilter("txt");
    public static final FileExtentionFilter YAML = new FileExtentionFilter("yml");
    public static final FileExtentionFilter JSON = new FileExtentionFilter("json");
    public static final FileExtentionFilter INI = new FileExtentionFilter("ini");
    public static final FileExtentionFilter JAR = new FileExtentionFilter("jar");
    private final String extention;

    public FileExtentionFilter(String extention)
    {
        if (!extention.startsWith("."))
        {
            extention = "." + extention;
        }
        this.extention = extention;
    }

    @Override
    public boolean accept(File file)
    {
        return (file.isFile() && file.getPath().endsWith(this.extention));
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
