package de.cubeisland.cubeengine.core.util.convert;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.*;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.converter.*;
import de.cubeisland.cubeengine.core.util.convert.converter.BooleanConverter;
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
import java.util.List;
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
     * @param clazz the class
     * @param converter the converter
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
    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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

    public static Node toNode(Object o) {
        if (o == null)
        {
            return NullNode.INSTANCE;
        }
        if (o instanceof Map)
        {
            return new MapNode((Map)o);
        }
        if (o instanceof Collection)
        {
            return new ListNode((List)o);
        }
        if (o.getClass().isArray())
        {
            return new ListNode((Object[])o);
        }
        if (o instanceof String)
        {
            return new StringNode((String)o);
        }
        if (o instanceof Byte || o.getClass() == byte.class)
        {
            return new ByteNode((Byte)o);
        }
        if (o instanceof Short || o.getClass() == short.class)
        {
            return new ShortNode((Short)o);
        }
        if (o instanceof Integer || o.getClass() == int.class)
        {
            return new IntNode((Integer) o);
        }
        if (o instanceof Long || o.getClass() == long.class)
        {
            return new LongNode((Long) o);
        }
        if (o instanceof Float || o.getClass() == float.class)
        {
            return new FloatNode((Float) o);
        }
        if (o instanceof Double || o.getClass() == double.class)
        {
            return new DoubleNode((Double) o);
        }
        if (o instanceof Boolean || o.getClass() == boolean.class)
        {
            return new BooleanNode((Boolean) o);
        }
        if (o instanceof Character || o.getClass() == char.class)
        {
            return new CharNode((Character) o);
        }
        return new ObjectNode(o);
    }
}
