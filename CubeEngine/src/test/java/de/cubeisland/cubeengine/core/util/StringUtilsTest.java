package de.cubeisland.cubeengine.core.util;


import junit.framework.TestCase;

public class StringUtilsTest extends TestCase
{
    public void testTrim()
    {
        final String testString = "blabla";
        final String whilespace = " \n\t\n";

        assertEquals("rtrim failed!", testString, StringUtils.rtrim(testString + whilespace));
        assertEquals("ltrim failed!", testString, StringUtils.ltrim(whilespace + testString));
        assertEquals("trim failed!", testString, StringUtils.trim(whilespace + testString + whilespace));
    }
}
