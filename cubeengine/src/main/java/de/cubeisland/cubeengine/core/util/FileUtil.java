package de.cubeisland.cubeengine.core.util;

public class FileUtil
{
    private static final char[] ILLEGAL_CHARACTERS = {
        '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'
    };

    public static boolean isValidFileName(String fileName)
    {
        for (Character c : ILLEGAL_CHARACTERS)
        {
            if (fileName.contains(c.toString()))
            {
                return false;
            }
        }
        return true;
    }
}
