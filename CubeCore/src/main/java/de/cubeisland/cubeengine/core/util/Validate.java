package de.cubeisland.cubeengine.core.util;

import java.io.File;

/**
 *
 * @author Phillip Schichtel
 */
public class Validate extends org.apache.commons.lang.Validate
{
    private Validate()
    {}

    public static void fileExists(File file, String message)
    {
        notNull(file, "The file must not be null!");
        if (!file.exists())
        {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isDir(File file, String message)
    {
        notNull(file, "The file must not be null!");
        if (!file.isDirectory())
        {
            throw new IllegalArgumentException(message);
        }
    }
}