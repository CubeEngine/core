package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.permission.Role;
import de.cubeisland.cubeengine.core.user.User;
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
 *
 * @author Phillip Schichtel
 */
public class Convert
{
    private final static ConcurrentHashMap<Class<?>, Converter<?>> CONVERTERS = new ConcurrentHashMap<Class<?>, Converter<?>>();
    private final static ConcurrentHashMap<Class<?>, GenericConverter<?>> GENERICCONVERTERS = new ConcurrentHashMap<Class<?>, GenericConverter<?>>();
    private final static GenericConverter arrayConverter;

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
        registerConverter(Level.class, new LevelConverter());
        registerConverter(Float.class, converter = new FloatConverter());
        registerConverter(float.class, converter);
        registerConverter(ItemStack.class, new ItemStackConverter());
        registerConverter(Enchantment.class, new EnchantmentConverter());
        registerConverter(User.class, new UserConverter());
        registerConverter(World.class, new WorldConverter());

        registerGenericConverter(Collection.class, new ColletionConverter());
        registerGenericConverter(Map.class, new MapConverter());
        arrayConverter = new ArrayConverter();
    }

    public static void registerConverter(Class<?> clazz, Converter<?> converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        CONVERTERS.put(clazz, converter);
    }

    public static void registerGenericConverter(Class<?> clazz, GenericConverter<?> converter)
    {
        if (clazz == null || converter == null)
        {
            return;
        }
        GENERICCONVERTERS.put(clazz, converter);
    }

    /**
     * Searches matching Converter
     *
     * @param objectClass the class to search for
     * @return a matching converter or null if not found
     */
    public static <T> Converter<T> matchConverter(Class<? extends T> objectClass)
    {
        if (CONVERTERS.containsKey(objectClass))
        {
            return (Converter<T>)CONVERTERS.get(objectClass);
        }
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

    /**
     * Searches matching GenericConverter
     *
     * @param objectClass the class to search for
     * @return a matching GenericConverter or null if not found
     */
    public static <T> GenericConverter<T> matchGenericConverter(Class<? extends T> objectClass)
    {
        if (objectClass.isArray())
        {
            return arrayConverter;
        }
        for (Map.Entry<Class<?>, GenericConverter<?>> entry : GENERICCONVERTERS.entrySet())
        {
            if (entry.getKey().isAssignableFrom(objectClass))
            {
                registerGenericConverter(objectClass, entry.getValue());
                return (GenericConverter<T>)entry.getValue();
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
        GenericConverter genericConverter = matchGenericConverter(object.getClass());
        return genericConverter.toObject(object, genericType);
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
        GenericConverter genericConverter = matchGenericConverter(fieldClass);
        return (T)genericConverter.fromObject(object, genericType);
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
        if (type == String.class)
        {
            return type.cast(string);
        }
        Converter<T> converter = matchConverter(type);
        if (converter != null)
        {
            return converter.fromString(string);
        }
        return null;
    }
}
