package de.cubeisland.cubeengine.core.util;

import junit.framework.TestCase;

public class StringUtilsTest extends TestCase
{
    public void testTrim()
    {
        final String testString = "blabla";
        final String whilespace = " \n\t\n";

        assertEquals("trimRight failed!", testString, StringUtils.trimRight(testString + whilespace));
        assertEquals("trimLeft failed!", testString, StringUtils.trimLeft(whilespace + testString));
        assertEquals("trim failed!", testString, StringUtils.trim(whilespace + testString + whilespace));
    }

    public void testStripFileExtention()
    {
        final String testString = "blabla";

        assertEquals("Something got stripped out even though there was not extention", testString, testString);
        assertEquals("Extention not properly stripped", testString, StringUtils.stripFileExtention(testString + ".test"));
    }
}
