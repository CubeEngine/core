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

import static de.cubeisland.engine.core.util.ChatFormat.*;
import static org.junit.Assert.assertEquals;

public class ChatFormatTest
{
    @Test
    public void testStripFormats() throws Exception
    {
        assertEquals(stripFormats(GOLD + "Gold"), "Gold");
    }

    @Test
    public void testStripRedundantFormats() throws Exception
    {
        assertEquals(stripRedundantFormats(WHITE + "" + GOLD + "Gold"), GOLD + "Gold");
    }

    @Test
    public void testParseFormats() throws Exception
    {
        assertEquals(parseFormats('&', "&" + GOLD.getChar() + "Gold"), BASE_CHAR + "" + GOLD.getChar() + "Gold");
    }
}
