/*
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
package org.cubeengine.libcube.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest
{
    @Test
    public void testTrim()
    {
        final String testString = "blabla";
        final String whilespace = " \n\t\n";

        assertEquals(testString, StringUtils.trimRight(testString + whilespace), "trimRight failed!");
        assertEquals(testString, StringUtils.trimLeft(whilespace + testString), "trimLeft failed!");
        assertEquals(testString, StringUtils.trim(whilespace + testString + whilespace), "trim failed!");
    }

    @Test
    public void testStripFileExtension()
    {
        final String testString = "blabla";

        assertEquals(testString, testString, "Something got stripped out even though there was not extention");
        assertEquals(testString, StringUtils.stripFileExtension(testString + ".test"), "Extention not properly stripped");
    }

    @Test
    public void replaceWithCallback()
    {
        AtomicInteger counter = new AtomicInteger(0);
        String actual = StringUtils.replaceWithCallback(Pattern.compile("a"), "aaaab", match -> match.group(0) + counter.getAndIncrement());

        assertEquals("a0a1a2a3b", actual);
    }
}
