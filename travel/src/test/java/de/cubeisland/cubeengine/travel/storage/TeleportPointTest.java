package de.cubeisland.cubeengine.travel.storage;

import junit.framework.TestCase;

public class TeleportPointTest extends TestCase
{
    public void testVisibilityEnum()
    {
        assertEquals(TeleportPoint.Visibility.PUBLIC.ordinal(), 0);
        assertEquals(TeleportPoint.Visibility.PRIVATE.ordinal(), 1);
    }

    public void testTypeEnum()
    {
        assertEquals(TeleportPoint.Type.HOME.ordinal(), 0);
        assertEquals(TeleportPoint.Type.WARP.ordinal(), 1);
    }
}
