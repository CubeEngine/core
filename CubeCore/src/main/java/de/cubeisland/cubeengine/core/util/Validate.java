package de.cubeisland.cubeengine.core.util;

import java.io.File;

/**
 *
 * @author Phillip Schichtel
 */
public class Validate
{
    public static void notNull(Object object, String message)
    {
        if (object == null)
        {
            throw new IllegalArgumentException(message);
        }
    }

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
