package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.permission.Role;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Phillip Schichtel
 */
public class Convert
{
    private final static ConcurrentHashMap<Class<?>, Converter<?>> CONVERTERS = new ConcurrentHashMap<Class<?>, Converter<?>>();

    static
    {
        Core core = CubeEngine.getCore();
        Converter<?> converter;

        registerConverter(OfflinePlayer.class, new PlayerConverter(core));
        registerConverter(Location.class, new LocationConverter(core));
        registerConverter(Set.class, new SetConverter());
        registerConverter(Integer.class, converter = new IntegerConverter());
        registerConverter(int.class, converter);
        registerConverter(Short.class, converter = new ShortConverter());
        registerConverter(short.class, converter);
        registerConverter(Byte.class, converter = new ByteConverter());
        registerConverter(byte.class, converter);
        registerConverter(Double.class, converter = new DoubleConverter());
        registerConverter(double.class, converter);
        registerConverter(Role.class, new RoleConverter());
        registerConverter(Date.class, new DateConverter());
    }

    public static void registerConverter(Class<?> clazz, Converter<?> converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        CONVERTERS.put(clazz, converter);
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
    public static <T> Converter<T> matchConverter(Class<? extends T> objectClass)
    {
        for (Map.Entry<Class<?>, Converter<?>> entry : CONVERTERS.entrySet())
        {
            if (entry.getKey().isAssignableFrom(objectClass))
            {
                registerConverter(objectClass, entry.getValue());
                return (Converter<T>)entry.getValue();
            }
        }
        return null;
    }

    public static <T> Object toObject(T object) throws ConversionException
    {
        Converter<T> converter = (Converter<T>)matchConverter(object.getClass());
        if (converter != null)
        {
            return converter.toObject(object);
        }
        return object;
    }

    public static <T> Object toObject(T object, Class<?> genericType) throws ConversionException
    {
        if (genericType == Object.class)
        {
            return object;//no GenericType
        }
        Converter converter = Convert.matchConverter(genericType);
        Class<?> objectClass = object.getClass();

        if (Collection.class.isAssignableFrom(objectClass))
        {
            if (converter != null)
            {
                Collection<?> collection = (Collection<?>)object;
                Collection<Object> result = new LinkedList<Object>();
                for (Object o : collection)
                {
                    result.add(converter.toObject(o));
                }
                return result;
            }
            return object;
        }
        if (Map.class.isAssignableFrom(objectClass))
        {

            if (converter != null)
            {
                Map<String, ?> map = (Map<String, ?>)object;
                Map<String, Object> result = new LinkedHashMap<String, Object>();
                for (String key : map.keySet())
                {
                    result.put(key, converter.toObject(map.get(key)));
                }
                return result;
            }
            return object;
        }
        if (objectClass.isArray())
        {
            Object[] array = (Object[])object;
            if (converter != null)
            {
                Collection<Object> result = new LinkedList<Object>();
                for (Object o : array)
                {
                    result.add(converter.toObject(o));
                }
                return result;
            }
            Collection<Object> result = new LinkedList<Object>();
            result.addAll(Arrays.asList(array));
            return result;
        }
        return object;
    }

    public static <T> T fromObject(Class<T> type, Object object) throws ConversionException
    {
        Converter<T> converter = matchConverter(type);
        if (converter != null)
        {
            return converter.fromObject(object);
        }
        return null;
    }

    public static <T> T fromObject(Class<T> fieldClass, Object fieldObject, Object object, Class<?> genericType) throws ConversionException
    {
        if (genericType == Object.class)
        {
            return (T)object;//no GenericType
        }
        Converter converter = matchConverter(genericType);

        if (Collection.class.isAssignableFrom(fieldClass))
        {
            if (converter != null)
            {
                Collection<?> list = (Collection<?>)object;
                if (list.isEmpty())
                {
                    return (T)object;
                }
                Collection<Object> result = (Collection<Object>)fieldObject;
                result.clear();
                for (Object o : list)
                {
                    result.add(converter.fromObject(o));
                }
                return (T)result;
            }
            return (T)object;
        }
        if (Map.class.isAssignableFrom(fieldClass))
        {
            if (converter != null)
            {
                Map<String, ?> map = (Map<String, ?>)object;
                if (map.isEmpty())
                {
                    return (T)object;
                }
                Map<String, Object> result = (Map<String, Object>)fieldObject;
                result.clear();
                for (Map.Entry<String, ?> entry : map.entrySet())
                {
                    result.put(entry.getKey(), converter.fromObject(entry.getValue()));
                }
                return (T)result;
            }
            return (T)object;
        }
        if (fieldClass.isArray())
        {
            Collection<Object> coll = (Collection)object;
            Object tmparray = coll.toArray();
            if (converter != null)
            {
                Object o = Array.newInstance(genericType, coll.size());
                for (int i = 0; i < coll.size(); ++i)
                {
                    Array.set(o, i, converter.fromObject(Array.get(tmparray, i)));
                }
                return (T)o;
            }
            Object o = Array.newInstance(genericType, coll.size());
            for (int i = 0; i < coll.size(); ++i)
            {
                Array.set(o, i, Array.get(tmparray, i));
            }
            return (T)o;
        }
        return (T)object;
    }

    public static <T> String toString(T object) throws ConversionException
    {
        Converter<T> converter = (Converter<T>)matchConverter(object.getClass());
        if (converter != null)
        {
            converter.toObject(object);
        }
        return null;
    }

    public static <T> T fromString(Class<T> type, String string) throws ConversionException
    {
        Converter<T> converter = matchConverter(type);
        if (converter != null)
        {
            return converter.fromString(string);
        }
        return null;
    }
}
