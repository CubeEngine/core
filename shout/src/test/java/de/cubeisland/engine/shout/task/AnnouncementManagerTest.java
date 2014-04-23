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
package de.cubeisland.engine.shout.task;

import junit.framework.TestCase;

import static de.cubeisland.engine.shout.announce.AnnouncementManager.parseDelay;

public class AnnouncementManagerTest extends TestCase
{
    public void testParseDelay()
    {
        assertEquals(1000l, parseDelay("1 second"));
        assertEquals(60000l, parseDelay("1 minute"));
        assertEquals(3600000l, parseDelay("1 hour"));
        assertEquals(86400000l, parseDelay("1 day"));

        assertEquals(172800000l, parseDelay("2 days"));
        assertEquals(2000l, parseDelay("2 seconds"));
        assertEquals(120000l, parseDelay("2 minutes"));
        assertEquals(7200000l, parseDelay("2 hours"));
    }
}
