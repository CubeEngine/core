package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.lang.reflect.Modifier;
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
        Converter keyConverter = Convert.matchConverter(keyType);
        Converter valConverter = Convert.matchConverter(valType);
        for (Object key : map.keySet())
        {
            result.put(keyConverter.toObject(key), valConverter.toObject(map.get(key)));
        }
        return result;
    }

    /**
     * Deserializes an object back to a map
     *
     * @param <K>     the KeyType
     * @param <V>     the ValueType
     * @param <S>     the MapType
     * @param mapType the MapTypeClass
     * @param object  the object to convert
     * @param keyType the KeyTypeClass
     * @param valType the ValueTypeClass
     * @return the converted map
     * @throws ConversionException
     */
    public <K, V, S extends Map<K, V>> S fromObject(Class<S> mapType, Object object, Class<K> keyType, Class<V> valType) throws ConversionException
    {
        try
        {
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
                Converter<K> keyConverter = Convert.matchConverter(keyType);
                Converter<V> valConverter = Convert.matchConverter(valType);
                Map<?, ?> objectmap = (Map<?, ?>)object;
                for (Object key : objectmap.keySet())
                {
                    result.put(keyConverter.fromObject(key), valConverter.fromObject(objectmap.get(key)));
                }
                return result;
            }
            throw new IllegalStateException("Map-conversion failed: Cannot convert not a map to a map.");
        }
        catch (IllegalAccessException ex)
        {
            throw new IllegalArgumentException("Map-conversion failed: Could not access the default constructor of: " + mapType.getName(), ex);
        }
        catch (InstantiationException ex)
        {
            throw new IllegalArgumentException("Map-conversion failed: Could not create an instance of: " + mapType.getName(), ex);
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Map-conversion failed: Error while converting the values in the map.", ex);
        }
    }
}