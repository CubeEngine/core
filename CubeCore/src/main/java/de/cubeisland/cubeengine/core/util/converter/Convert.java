package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.CubeEngine;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.permission.Role;
import java.sql.Date;
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

    public static <T> T fromObject(Class<T> type, Object object) throws ConversionException
    {
        Converter<T> converter = matchConverter(type);
        if (converter != null)
        {
            return converter.fromObject(object);
        }
        return null;
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