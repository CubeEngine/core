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
package de.cubeisland.cubeengine.core.util;

import java.lang.reflect.Method;

/**
 * This class is a wrapper for a method.
 */
public class Callback
{
    private final Object holder;
    private final Method method;

    public Callback(Object holder, Method method)
    {
        method.setAccessible(true);
        this.holder = holder;
        this.method = method;
    }

    public static Callback createCallback(Object holder, String methodName, Class... argTyps)
    {
        try
        {
            return new Callback(holder, holder.getClass().getDeclaredMethod(methodName, argTyps));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Object call(Object... args)
    {
        try
        {
            return method.invoke(holder, args);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
