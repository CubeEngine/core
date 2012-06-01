package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author Phillip Schichtel
 */
public class FileExtentionFilter implements FileFilter
{
    private final String extention;

    public FileExtentionFilter(String extention)
    {
        if (!extention.startsWith("."))
        {
            extention = "." + extention;
        }
        this.extention = extention;
    }

    public boolean accept(File file)
    {
        return (file.isFile() && file.getPath().endsWith(this.extention));
    }
}
