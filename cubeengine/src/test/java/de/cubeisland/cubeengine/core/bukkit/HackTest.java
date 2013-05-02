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
package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.TestCore;
import junit.framework.TestCase;
import net.minecraft.server.v1_5_R3.Connection;
import net.minecraft.server.v1_5_R3.Packet;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class HackTest extends TestCase
{
    @Override
    public void setUp() throws Exception
    {
        CubeEngine.initialize(new TestCore());
    }

    public void testImplementedPacketMethods()
    {
        Method[] nhMethods = readMethods(Connection.class);
        Method[] ceMethods = readMethods(CubePlayerConnection.class);

        System.out.println("nh methods: " + nhMethods.length);
        System.out.println("ce methods: " + ceMethods.length);

        assertEquals("Not all packet methods are implemented.", nhMethods.length, ceMethods.length);
    }

    //    public void testBukkitUtils()
    //    {
    //        assertTrue("The BukkitUtils couldn't find a field!", BukkitUtils.isCompatible());
    //    }

    private static Method[] readMethods(final Class clazz)
    {
        List<Method> methods = new ArrayList<Method>();

        for (Method method : clazz.getDeclaredMethods())
        {
            if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers()) || Modifier.isAbstract(method.getModifiers()))
            {
                continue;
            }

            if (method.getParameterTypes().length != 1 || !Packet.class.isAssignableFrom(method.getParameterTypes()[0]))
            {
                continue;
            }

            methods.add(method);
        }

        return methods.toArray(new Method[methods.size()]);
    }
}
