package de.cubeisland.cubeengine.core.util.converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public class MapConverter implements GenericConverter<Map>
{
    public Object toObject(Map object, Class<?> genericType) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        if (converter != null)
        {
            Map<String, ?> map = (Map<String, ?>)object;
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            for (String key : map.keySet())
            {
                result.put(key, converter.toObject(map.get(key)));
            }
            return result;
        }
        return object;

    }

    public <G> Map fromObject(Object object, Class<G> genericType) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        if (converter != null)
        {
            Map<String, ?> map = (Map<String, ?>)object;
            if (map.isEmpty())
            {
                return (Map<String, Object>)object;
            }
            Map<String, G> result = new LinkedHashMap<String, G>();
            for (Map.Entry<String, ?> entry : map.entrySet())
            {
                result.put(entry.getKey(), (G)converter.fromObject(entry.getValue()));
            }
            return result;
        }
        return (Map<String, G>)object;
    }
}
