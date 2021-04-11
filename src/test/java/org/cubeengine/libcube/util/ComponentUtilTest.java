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

import net.kyori.adventure.text.Component;
import org.junit.Test;

import static net.kyori.adventure.text.Component.*;
import static org.junit.Assert.*;

public class ComponentUtilTest
{
    @Test
    public void autoLink()
    {
        final String hover = "open";
        final Component link = ComponentUtil.clickableLink("http://test", "http://test", hover);

        assertEquals(link, ComponentUtil.autoLink("http://test", hover));
        assertEquals(empty(), ComponentUtil.autoLink("", hover));
        assertEquals(join(empty(), space(), link, space()), ComponentUtil.autoLink(" http://test ", hover));
        assertEquals(join(empty(), space(), link, space(), link, space()), ComponentUtil.autoLink(" http://test http://test ", hover));
    }
}