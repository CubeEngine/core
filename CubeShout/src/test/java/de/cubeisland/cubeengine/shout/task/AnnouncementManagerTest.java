package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import java.io.File;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnnouncementManagerTest
{
    // TODO test the actual class, not single methods
    @Test
    public void testParseDelay()
    {
        AnnouncementManager testamanager = new AnnouncementManager(new Shout(), null);

        assertEquals(1000l, testamanager.parseDelay("1 second"));
        assertEquals(60000l, testamanager.parseDelay("1 minute"));
        assertEquals(3600000l, testamanager.parseDelay("1 hour"));
        assertEquals(86400000l, testamanager.parseDelay("1 day"));

        assertEquals(172800000l, testamanager.parseDelay("2 days"));
        assertEquals(2000l, testamanager.parseDelay("2 seconds"));
        assertEquals(120000l, testamanager.parseDelay("2 minutes"));
        assertEquals(7200000l, testamanager.parseDelay("2 hours"));
    }
}
