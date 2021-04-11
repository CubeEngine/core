package org.cubeengine.libcube.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentUtilTest
{
    @Test
    public void autoLink()
    {
        assertEquals(ComponentUtil.clickableLink("http://test", "http://test", "open"), ComponentUtil.autoLink("http://test", "open"));
    }
}