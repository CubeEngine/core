package de.cubeisland.cubeengine.core.util;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: Phillip
 * Date: 30.05.13
 * Time: 20:32
 * To change this template use File | Settings | File Templates.
 */
public class ReflectionUtils
{
    public static Field getField(Class<?> clazz, String name)
    {
        Field field = null;
        try
        {
            clazz.getDeclaredField(name);
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
}
