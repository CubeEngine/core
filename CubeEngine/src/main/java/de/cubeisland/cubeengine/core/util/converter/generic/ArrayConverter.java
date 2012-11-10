package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class ArrayConverter
{
    public Object toObject(Object[] array) throws ConversionException
    {
        Collection<Object> result = new LinkedList<Object>();
        if (array.length == 0)
        {
            return result;
        }
        Converter valConverter = Convert.matchConverter(Arrays.asList(array).iterator().next().getClass());
        for (Object value : array)
        {
            result.add(valConverter.toObject(value));
        }
        return result;
    }

    public <V> V[] fromObject(Object object, Class<V> valType) throws ConversionException
    {
        Collection<V> result = new LinkedList<V>();
        if (object instanceof Collection)
        {
            Converter<V> valConverter = Convert.matchConverter(valType);
            Collection col = (Collection)object;
            for (Object o : col)
            {
                result.add(valConverter.fromObject(o));
            }
            return result.toArray((V[])Array.newInstance(valType, result.size()));
        }
        else
        {
            throw new IllegalStateException("Cannot convert not a collection to an array.");
        }
    }
}