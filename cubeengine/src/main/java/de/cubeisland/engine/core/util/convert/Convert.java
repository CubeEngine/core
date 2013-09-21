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
package de.cubeisland.engine.core.util.convert;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.config.node.BooleanNode;
import de.cubeisland.engine.core.config.node.ByteNode;
import de.cubeisland.engine.core.config.node.CharNode;
import de.cubeisland.engine.core.config.node.DoubleNode;
import de.cubeisland.engine.core.config.node.FloatNode;
import de.cubeisland.engine.core.config.node.IntNode;
import de.cubeisland.engine.core.config.node.ListNode;
import de.cubeisland.engine.core.config.node.LongNode;
import de.cubeisland.engine.core.config.node.MapNode;
import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.NullNode;
import de.cubeisland.engine.core.config.node.ShortNode;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.util.convert.converter.BooleanConverter;
import de.cubeisland.engine.core.util.convert.converter.ByteConverter;
import de.cubeisland.engine.core.util.convert.converter.LevelConverter;
import de.cubeisland.engine.core.util.convert.converter.DateConverter;
import de.cubeisland.engine.core.util.convert.converter.DoubleConverter;
import de.cubeisland.engine.core.util.convert.converter.DurationConverter;
import de.cubeisland.engine.core.util.convert.converter.EnchantmentConverter;
import de.cubeisland.engine.core.util.convert.converter.FloatConverter;
import de.cubeisland.engine.core.util.convert.converter.IntegerConverter;
import de.cubeisland.engine.core.util.convert.converter.ItemStackConverter;
import de.cubeisland.engine.core.util.convert.converter.LocaleConverter;
import de.cubeisland.engine.core.util.convert.converter.LocationConverter;
import de.cubeisland.engine.core.util.convert.converter.LongConverter;
import de.cubeisland.engine.core.util.convert.converter.MaterialConverter;
import de.cubeisland.engine.core.util.convert.converter.PlayerConverter;
import de.cubeisland.engine.core.util.convert.converter.ShortConverter;
import de.cubeisland.engine.core.util.convert.converter.StringConverter;
import de.cubeisland.engine.core.util.convert.converter.UserConverter;
import de.cubeisland.engine.core.util.convert.converter.VersionConverter;
import de.cubeisland.engine.core.util.convert.converter.WorldConverter;
import de.cubeisland.engine.core.util.convert.converter.generic.ArrayConverter;
import de.cubeisland.engine.core.util.convert.converter.generic.CollectionConverter;
import de.cubeisland.engine.core.util.convert.converter.generic.MapConverter;
import de.cubeisland.engine.core.util.time.Duration;

import ch.qos.logback.classic.Level;

/**
 * This class provides the converters.
 */
public class Convert
{
    private static Map<Class, Converter> converters;
    private static MapConverter mapConverter;
    private static ArrayConverter arrayConverter;
    private static CollectionConverter collectionConverter;

    public synchronized static void init(Core core)
    {
        if (converters != null)
        {
            return;
        }

        converters = new ConcurrentHashMap<>();
        mapConverter = new MapConverter();
        arrayConverter = new ArrayConverter();
        collectionConverter = new CollectionConverter();

        Converter<?> converter;

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
        registerConverter(Material.class, new MaterialConverter());
        registerConverter(Enchantment.class, new EnchantmentConverter());
        registerConverter(User.class, new UserConverter());
        registerConverter(World.class, new WorldConverter());
        registerConverter(boolean.class, converter = new BooleanConverter());
        registerConverter(Boolean.class, converter);
        registerConverter(String.class, new StringConverter());
        registerConverter(Duration.class, new DurationConverter());
        registerConverter(Locale.class, new LocaleConverter());
        registerConverter(Version.class, new VersionConverter());
        registerConverter(OfflinePlayer.class, new PlayerConverter(core));
        registerConverter(Location.class, new LocationConverter(core));
    }

    public synchronized static void cleanup()
    {
        removeConverters();
        converters = null;
        mapConverter = null;
        arrayConverter = null;
        collectionConverter = null;
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
        converters.put(clazz, converter);
    }

    public static void removeConverter(Class clazz)
    {
        Iterator<Map.Entry<Class, Converter>> iter = converters.entrySet().iterator();

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

    public static void removeConverters()
    {
        converters.clear();
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
        Converter converter = converters.get(objectClass);
        if (converter == null)
        {
            for (Map.Entry<Class, Converter> entry : converters.entrySet())
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

    /**
     * Wraps a serialized Object into a Node
     *
     * @param o a serialized Object
     * @return the Node
     */
    public static Node wrapIntoNode(Object o)
    {
        if (o == null)
        {
            return NullNode.emptyNode();
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
            return new ByteNode((byte)o);
        }
        if (o instanceof Short || o.getClass() == short.class)
        {
            return new ShortNode((short)o);
        }
        if (o instanceof Integer || o.getClass() == int.class)
        {
            return new IntNode((int)o);
        }
        if (o instanceof Long || o.getClass() == long.class)
        {
            return new LongNode((long)o);
        }
        if (o instanceof Float || o.getClass() == float.class)
        {
            return new FloatNode((float)o);
        }
        if (o instanceof Double || o.getClass() == double.class)
        {
            return new DoubleNode((double)o);
        }
        if (o instanceof Boolean || o.getClass() == boolean.class)
        {
            return new BooleanNode((boolean)o);
        }
        if (o instanceof Character || o.getClass() == char.class)
        {
            return new CharNode((char)o);
        }
        throw new IllegalArgumentException("Cannot wrap into Node: " + o.getClass());

    }

    /**
     * Converts a convertible Object into a Node
     *
     * @param object the Object
     * @return the serialized Node
     */
    public static <T> Node toNode(T object) throws ConversionException
    {
        if (object == null)
        {
            return null;
        }
        if (object.getClass().isArray())
        {
            return arrayConverter.toNode((Object[])object);
        }
        else if (object instanceof Collection)
        {
            return collectionConverter.toNode((Collection)object);
        }
        else if (object instanceof Map)
        {
            return mapConverter.toNode((Map)object);
        }
        Converter<T> converter = (Converter<T>)matchConverter(object.getClass());
        return converter.toNode(object);
    }

    /**
     * Converts a Node back into the original Object
     *
     * @param node the node
     * @param type the type of the object
     * @return
     */
    public static <T> T fromNode(Node node, Type type) throws ConversionException
    {
        if (node == null || node instanceof NullNode || type == null)
            return null;
        if (type instanceof Class)
        {
            if (((Class)type).isArray())
            {
                if (node instanceof ListNode)
                {
                    return (T)arrayConverter.fromNode((Class<T[]>)type, (ListNode)node);
                }
                else
                {
                    throw new ConversionException("Cannot convert to Array! Node is not a ListNode!");
                }
            }
            else
            {
                Converter<T> converter = matchConverter((Class<T>)type);
                return converter.fromNode(node);
            }
        }
        else if (type instanceof ParameterizedType)
        {
            ParameterizedType ptype = (ParameterizedType)type;
            if (ptype.getRawType() instanceof Class)
            {
                if (Collection.class.isAssignableFrom((Class)ptype.getRawType()))
                {
                    if (node instanceof ListNode)
                    {
                        return (T)collectionConverter.fromNode(ptype, (ListNode)node);
                    }
                    else
                    {
                        throw new ConversionException("Cannot convert to Collection! Node is not a ListNode!");
                    }

                }
                else if (Map.class.isAssignableFrom((Class)ptype.getRawType()))
                {
                    if (node instanceof MapNode)
                    {
                        return (T)mapConverter.fromNode(ptype, (MapNode)node);
                    }
                    else
                    {
                        throw new ConversionException("Cannot convert to Map! Node is not a MapNode!");
                    }

                }
            }
        }
        throw new IllegalArgumentException("Unknown Type: " + type);
    }
}
