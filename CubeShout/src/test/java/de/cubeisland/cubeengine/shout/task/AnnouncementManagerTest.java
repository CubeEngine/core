package de.cubeisland.cubeengine.shout.task;

import org.junit.Test;

import static de.cubeisland.cubeengine.shout.announce.AnnouncementManager.parseDelay;
import static org.junit.Assert.assertEquals;

public class AnnouncementManagerTest
{
    // TODO test the actual class, not single methods
    @Test
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
