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
package de.cubeisland.engine.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest
{
    @Test
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
        assertEquals("Extention not properly stripped", testString, StringUtils.stripFileExtension(testString + ".test"));
    }
}
