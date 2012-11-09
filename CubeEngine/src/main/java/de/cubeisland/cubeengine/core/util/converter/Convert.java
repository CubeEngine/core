package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.converter.generic.ArrayConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.CollectionConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.GenericConverter;
import de.cubeisland.cubeengine.core.util.converter.generic.MapConverter;
import java.sql.Date;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * This class provides the conveters.
 */
public class Convert
{
    private final static ConcurrentHashMap<Class<?>, Converter<?>> CONVERTERS = new ConcurrentHashMap<Class<?>, Converter<?>>();
    public final static MapConverter MAPCONVERTER = new MapConverter();
    public final static ArrayConverter ARRAYCONVERTER = new ArrayConverter();
    public final static CollectionConverter COLLECTIONCONVERTER = new CollectionConverter();

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
        registerConverter(Level.class, new LevelConverter());
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
    }

    /**
     * registers a converter to check for when converting
     *
     * @param clazz
     * @param converter
     */
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
        Converter converter = CONVERTERS.get(objectClass);
        if (converter == null)
        {
            for (Map.Entry<Class<?>, Converter<?>> entry : CONVERTERS.entrySet())
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
        throw new IllegalStateException("Converter not found for: " + objectClass.getCanonicalName());
    }

    public static <T> Object toObject(T object) throws ConversionException
    {
        if (object == null)
        {
            return null;
        }
        if (object.getClass().isArray())
        {
            return ARRAYCONVERTER.toObject((Object[])object);
        }
        else if (object instanceof Collection)
        {
            return COLLECTIONCONVERTER.toObject((Collection)object);
        }
        else if (object instanceof Map)
        {
            return MAPCONVERTER.toObject((Map)object);
        }
        Converter<T> converter = (Converter<T>)matchConverter(object.getClass());
        return converter.toObject(object);
    }

    public static <T> T fromObject(Class<T> type, Object object) throws ConversionException
    {
        Converter<T> converter = matchConverter(type);
        return converter.fromObject(object);
    }

    public static <K, V> Map<K, V> fromObjectToMap(Class<K> keyType, Class<V> valType, Object object) throws ConversionException
    {
        return MAPCONVERTER.fromObject(object, keyType, valType);
    }

    public static <V> Collection<V> fromObjectToCollection(Class<V> valType, Object object) throws ConversionException
    {
        return COLLECTIONCONVERTER.fromObject(object, valType);
    }

    public static <V> V[] fromObjectToArray(Class<V> valType, Object object) throws ConversionException
    {
        return ARRAYCONVERTER.fromObject(object, valType);
    }
}