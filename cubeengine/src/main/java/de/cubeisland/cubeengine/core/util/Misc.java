package de.cubeisland.cubeengine.core.util;

public final class Misc
{
    private Misc()
    {}

    public static <T> T[] arr(T... objects)
    {
        return objects;
    }

    public static boolean isNumeric(String string)
    {
        if (string == null)
        {
            throw new NullPointerException("The string must not be null!");
        }
        final int len = string.length();
        if (len == 0)
        {
            return false;
        }
        for (int i = 0; i < len; ++i)
        {
            if (!Character.isDigit(string.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }
}
