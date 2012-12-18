package de.cubeisland.cubeengine.core.util.convert;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.converter.BooleanConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.ByteConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.CubeLevelConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.DateConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.DoubleConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.DurationConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.EnchantmentConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.FloatConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.IntegerConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.ItemStackConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.LocationConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.LongConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.PlayerConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.ShortConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.StringConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.UserConverter;
import de.cubeisland.cubeengine.core.util.convert.converter.WorldConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.ArrayConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.CollectionConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.MapConverter;
import de.cubeisland.cubeengine.core.util.log.CubeLevel;
import de.cubeisland.cubeengine.core.util.time.Duration;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides the converters.
 */
public class Convert
{
    private final static Map<Class, Converter> CONVERTERS = new ConcurrentHashMap<Class, Converter>();
    private final static MapConverter MAP_CONVERTER = new MapConverter();
    private final static ArrayConverter ARRAY_CONVERTER = new ArrayConverter();
    private final static CollectionConverter COLLECTION_CONVERTER = new CollectionConverter();

    static
    {
        Core core = CubeEngine.getCore();
        Converter<?> converter;

        registerConverter(OfflinePlayer.class, new PlayerConverter(core));
        registerConverter(Location.class, new LocationConverter(core));
        registerConverter(Integer.class, converter = new IntegerConverter());
        registerConverter(int.class, converter);
        registerConverter(Short.class, converter = new ShortConverter());
        registerConverter(short.class, converter);
        registerConverter(Byte.class, converter = new ByteConverter());
        registerConverter(byte.class, converter);
        registerConverter(Double.class, converter = new DoubleConverter());
        registerConverter(double.class, converter);
        registerConverter(Date.class, new DateConverter());
        registerConverter(CubeLevel.class, new CubeLevelConverter());
        registerConverter(Float.class, converter = new FloatConverter());
        registerConverter(float.class, converter);
        registerConverter(Long.class, converter = new LongConverter());
        registerConverter(long.class, converter);
        registerConverter(ItemStack.class, new ItemStackConverter());
        registerConverter(Enchantment.class, new EnchantmentConverter());
        registerConverter(User.class, new UserConverter());
        registerConverter(World.class, new WorldConverter());
        registerConverter(boolean.class, converter = new BooleanConverter());
        registerConverter(Boolean.class, converter);
        registerConverter(String.class, new StringConverter());
        registerConverter(Duration.class, new DurationConverter());
    }

    /**
     * registers a converter to check for when converting
     *
     * @param clazz
     * @param converter
     */
    public static void registerConverter(Class clazz, Converter converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        CONVERTERS.put(clazz, converter);
    }

    public static void unregisterConverter(Class clazz)
    {
        Iterator<Map.Entry<Class, Converter>> iter = CONVERTERS.entrySet().iterator();

        Map.Entry<Class, Converter> entry;
        while (iter.hasNext())
        {
            entry = iter.next();
            if (entry.getKey() == clazz || entry.getValue().getClass() == clazz)
            {
                iter.remove();
            }
        }
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
    public static <T> Converter<T> matchConverter(Class<? extends T> objectClass)
    {
        if (objectClass == null)
        {
            return null;
        }
        Converter converter = CONVERTERS.get(objectClass);
        if (converter == null)
        {
            for (Map.Entry<Class, Converter> entry : CONVERTERS.entrySet())
            {
                if (entry.getKey().isAssignableFrom(objectClass))
                {
                    registerConverter(objectClass, converter = entry.getValue());
                    break;
                }
            }
        }
        if (converter != null)
        {
            return (Converter<T>)converter;
        }
        if (objectClass.isArray() || Collection.class.isAssignableFrom(objectClass) || Map.class.isAssignableFrom(objectClass))
        {
            return null;
        }
        throw new ConverterNotFoundException("Converter not found for: " + objectClass.getName());
    }

    public static <T> Object toObject(T object) throws ConversionException
    {
        if (object == null)
        {
            return null;
        }
        if (object.getClass().isArray())
        {
            return ARRAY_CONVERTER.toObject((Object[])object);
        }
        else if (object instanceof Collection)
        {
            return COLLECTION_CONVERTER.toObject((Collection)object);
        }
        else if (object instanceof Map)
        {
            return MAP_CONVERTER.toObject((Map)object);
        }
        Converter<T> converter = (Converter<T>)matchConverter(object.getClass());
        return converter.toObject(object);
    }

    public static <T> T fromObject(Type type, Object object) throws ConversionException
    {
        if (type == null)
        {
            return null;
        }
        if (type instanceof Class)
        {
            if (((Class)type).isArray())
            {
                return (T)ARRAY_CONVERTER.fromObject((Class<T[]>)type, object);
            }
            else
            {
                Converter<T> converter = matchConverter((Class<T>)type);
                return converter.fromObject(object);
            }
        }
        else if (type instanceof ParameterizedType)
        {
            ParameterizedType ptype = (ParameterizedType)type;
            if (ptype.getRawType() instanceof Class)
            {
                if (Collection.class.isAssignableFrom((Class)ptype.getRawType()))
                {
                    return (T)COLLECTION_CONVERTER.fromObject(ptype, object);
                }
                else if (Map.class.isAssignableFrom((Class)ptype.getRawType()))
                {
                    return (T)MAP_CONVERTER.fromObject(ptype, object);
                }
            }
        }
        throw new IllegalArgumentException("Unknown Type: " + type);
    }
}
