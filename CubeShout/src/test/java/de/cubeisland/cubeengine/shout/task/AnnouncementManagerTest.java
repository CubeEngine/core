package de.cubeisland.cubeengine.shout.task;

import static org.junit.Assert.assertEquals;

import de.cubeisland.cubeengine.shout.Shout;
import org.junit.Test;

public class AnnouncementManagerTest {
	
	@Test
	public void testParseDelay()
	{
		AnnouncementManager testamanager = new AnnouncementManager(new Shout());
		
		assertEquals(1000l, testamanager.parseDelay("1 secound"));
		assertEquals(60000l, testamanager.parseDelay("1 minute"));
		assertEquals(3600000l, testamanager.parseDelay("1 hour"));
		assertEquals(86400000l, testamanager.parseDelay("1 day"));
		
		assertEquals(172800000l, testamanager.parseDelay("2 days"));
		assertEquals(2000l, testamanager.parseDelay("2 secounds"));
		assertEquals(120000l, testamanager.parseDelay("2 minutes"));
		assertEquals(7200000l, testamanager.parseDelay("2 hours"));
	}
	
}
