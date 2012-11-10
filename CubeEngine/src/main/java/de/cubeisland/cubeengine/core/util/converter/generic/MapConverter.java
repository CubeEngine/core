package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
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
     * @param <K> the KeyType
     * @param <V> the ValueType
     * @param object the object to convert
     * @param keyType the KeyTypeClass
     * @param valType the ValueTypeClass
     * @return the converted map
     * @throws ConversionException 
     */
    public <K, V> Map<K, V> fromObject(Object object, Class<K> keyType, Class<V> valType) throws ConversionException
    {
        Map<K, V> result = new LinkedHashMap<K, V>();
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
        throw new IllegalStateException("Cannot convert not a map to a map.");
    }
}