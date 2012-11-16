package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

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
        Class<?> keyType = map.entrySet().iterator().next().getKey().getClass();
        Class<?> valType = map.entrySet().iterator().next().getValue().getClass();
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
     * @param keyType the KeyTypeClass
     * @param valType the ValueTypeClass
     * @return the converted map
     * @throws ConversionException
     */
    public <K, V, S extends Map<K, V>> S fromObject(ParameterizedType ptype, Object object) throws ConversionException
    {
        try
        {
            if (ptype.getRawType() instanceof Class)
            {
                Class<S> mapType = (Class)ptype.getRawType();
                Type keyType = ptype.getActualTypeArguments()[0];
                Type valType = ptype.getActualTypeArguments()[1];
                S result;
                if (mapType.isInterface() || Modifier.isAbstract(mapType.getModifiers()))
                {
                    result = (S)new LinkedHashMap<K, V>();
                }
                else
                {
                    result = mapType.newInstance();
                }
                if (object instanceof Map)
                {
                    Map<?, ?> objectmap = (Map<?, ?>)object;
                    for (Object key : objectmap.keySet())
                    {
                        K newkey = Convert.fromObject(keyType, key);
                        V newVal = Convert.fromObject(valType, objectmap.get(key));
                        result.put(newkey, newVal);
                    }
                    return result;
                }
                throw new IllegalStateException("Map-conversion failed: Cannot convert not a map to a map.");
            }
            throw new IllegalArgumentException("Unkown Map-Type: " + ptype);
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Map-conversion failed: Could not access the default constructor of: " + ptype.getRawType(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Map-conversion failed: Could not create an instance of: " + ptype.getRawType(), ex);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Map-conversion failed: Error while converting the values in the map.", ex);
        }
    }
}
