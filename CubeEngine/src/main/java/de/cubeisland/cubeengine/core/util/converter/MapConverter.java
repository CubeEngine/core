package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.config.Configuration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Anselm Brehme
 */
public class MapConverter implements GenericConverter<Map>
{
    @Override
    public Object toObject(Map object, Class<?> genericType, String basepath) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        if (converter != null)
        {
            Map<String, ?> map = (Map<String, ?>)object;
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            for (String key : map.keySet())
            {
                if (converter instanceof ConfigurationConverter)
                {
                    result.put(key, ((ConfigurationConverter)converter).toObject((Configuration)map.get(key), basepath));
                }
                else
                {
                    result.put(key, converter.toObject(map.get(key)));
                }
            }
            return result;
        }
        return object;

    }

    @Override
    public <G> Map fromObject(Object object, Object fieldObject, Class<G> genericType) throws ConversionException
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
                if (converter instanceof ConfigurationConverter)
                {
                    result.put(entry.getKey(), (G)((ConfigurationConverter)converter).fromObject(entry.getValue(), (Configuration)((Map)fieldObject).get(entry.getKey())));
                }
                else
                {
                    result.put(entry.getKey(), (G)converter.fromObject(entry.getValue()));
                }
            }
            return result;
        }
        return (Map<String, G>)object;
    }
}
