package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class MapConverter
{
    /**
     * Makes a map serializable for configs
     *
     * @param map the map to convert
     * @return the serializable map
     * @throws ConversionException
     */
    public Object toObject(Map<?, ?> map) throws ConversionException
    {
        Map<Object, Object> result = new LinkedHashMap<Object, Object>();
        if (map.isEmpty())
        {
            return result;
        }
        for (Object key : map.keySet())
        {
            result.put(Convert.toObject(key), Convert.toObject(map.get(key)));
        }
        return result;
    }

    /**
     * Deserializes an object back to a map
     *
     * @param <K>     the KeyType
     * @param <V>     the ValueType
     * @param <S>     the MapType
     * @param ptype   the MapTypeClass
     * @param object  the object to convert
     * @return the converted map
     * @throws ConversionException
     */
    @SuppressWarnings("unchecked")
    public <K, V, S extends Map<K, V>> S fromObject(ParameterizedType ptype, Object object) throws ConversionException
    {
        try
        {
            if (ptype.getRawType() instanceof Class)
            {

                Type keyType = ptype.getActualTypeArguments()[0];
                Type valType = ptype.getActualTypeArguments()[1];
                S result = (S)getMapFor(ptype);
                if (object instanceof Map)
                {
                    Map<?, ?> objectMap = (Map<?, ?>)object;
                    for (Object key : objectMap.keySet())
                    {
                        K newKey = Convert.fromObject(keyType, key);
                        V newVal = Convert.fromObject(valType, objectMap.get(key));
                        result.put(newKey, newVal);
                    }
                    return result;
                }
                throw new IllegalStateException("Map-conversion failed: Cannot convert not a map to a map.");
            }
            throw new IllegalArgumentException("Unkown Map-Type: " + ptype);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Map-conversion failed: Error while converting the values in the map.", ex);
        }
    }

    public static <K, V, S extends Map<K, V>> S getMapFor(ParameterizedType ptype) {
        try
        {
            Class<S> mapType = (Class<S>)ptype.getRawType();
            if (mapType.isInterface() || Modifier.isAbstract(mapType.getModifiers()))
            {
                return (S)new LinkedHashMap<K, V>();
            }
            else
            {
                return mapType.newInstance();
            }
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not access the default constructor of: " + ptype.getRawType(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Collection-conversion failed: Could not create an instance of: " + ptype.getRawType(), ex);
        }
    }
}
