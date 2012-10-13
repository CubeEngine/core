package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.core.util.converter.Converter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapConverter implements GenericConverter<Map>
{
    @Override
    public Object toObject(Map object, Class<?> genericType, String basepath) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        if (converter != null)
        {
            Map<Object, ?> map = (Map<Object, ?>)object;
            Map<String, Object> result = new LinkedHashMap<String, Object>();
            for (Object key : map.keySet())
            {
                result.put(key.toString(), converter.toObject(map.get(key)));
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
                result.put(entry.getKey(), (G)converter.fromObject(entry.
                    getValue()));
            }
            return result;
        }
        return (Map<String, G>)object;
    }
}
