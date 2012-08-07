package de.cubeisland.cubeengine.core.persistence.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 *
 * @author Phillip Schichtel
 */
public class FileExtentionFilter implements FileFilter, FilenameFilter
{
    public static final FileExtentionFilter YAML = new FileExtentionFilter("yaml");
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

    public boolean accept(File file)
    {
        return (file.isFile() && file.getPath().endsWith(this.extention));
    }

    public boolean accept(File dir, String name)
    {
        return name.endsWith(this.extention);
    }

    public String getExtention()
    {
        return this.extention;
    }
}