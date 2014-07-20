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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class ReflectionUtils
{
    public static Field getField(Class<?> clazz, String name)
    {
        Field field = null;
        try
        {
            field = clazz.getDeclaredField(name);
            field.setAccessible(true);
        }
        catch (Exception ignored)
        {}
        return field;
    }

    public static <T> T getFieldValue(Object holder, String fieldName, Class<T> type)
    {
        return getFieldValue(holder, getField(holder.getClass(), fieldName), type);
    }

    public static <T> T getStaticFieldValue(Class<?> holder, String fieldName, Class<T> type)
    {
        return getFieldValue(null, getField(holder.getClass(), fieldName), type);
    }

    public static <T> T getFieldValue(Object holder, Field field, Class<T> type)
    {
        if (field == null || type == null)
        {
            return null;
        }
        T value = null;
        try
        {
            field.setAccessible(true);
            Object temp = field.get(holder);
            if (temp == null)
            {
                return null;
            }
            if (type.isAssignableFrom(temp.getClass()))
            {
                value = type.cast(temp);
            }
        }
        catch (Exception ignored)
        {}

        return value;
    }

    public static Field findFirstField(Object holder, Class type)
    {
        return findFirstField(holder.getClass(), type);
    }

    public static Field findFirstField(Class holder, Class<?> type)
    {
        return findFirstField(holder, type, 0);
    }

    public static Field findFirstField(Object holder, Class<?> type, int superLevels)
    {
        return findFirstField(holder.getClass(), type, superLevels);
    }

    public static Field findFirstField(Class holder, Class<?> type, int superLevels)
    {
        expect(superLevels >= 0, "The super levels must be positive!");

        do
        {
            for (Field field : holder.getDeclaredFields())
            {
                if (type.isAssignableFrom(field.getType()))
                {
                    field.setAccessible(true);
                    return field;
                }
            }
        }
        while ((holder = holder.getSuperclass()) != null && superLevels-- > 0);

        return null;
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes)
    {
        try
        {
            return clazz.getConstructor(parameterTypes);
        }
        catch (ReflectiveOperationException ignored)
        {}
        return null;
    }
    
    public static <T> Constructor<T> getAccessibleConstructor(Class<T> clazz, Class<?>... parameterTypes)
    {
        Constructor<T> constructor = getConstructor(clazz, parameterTypes);
        if (constructor != null)
        {
            constructor.setAccessible(true);
        }
        return constructor;
    }
}
