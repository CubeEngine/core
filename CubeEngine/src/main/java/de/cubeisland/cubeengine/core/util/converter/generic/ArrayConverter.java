package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import java.lang.reflect.Array;
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
        for (Object value : array)
        {
            result.add(Convert.toObject(value));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <V> V[] fromObject(Class<V[]> arrayType, Object object) throws ConversionException
    {
        Class<V> valueType = (Class<V>)arrayType.getComponentType();
        try
        {
            Collection<V> result = new LinkedList<V>();
            if (object instanceof Collection)
            {
                for (Object o : (Collection)object)
                {
                    V value = Convert.fromObject(valueType, o);
                    result.add(value);
                }
                return result.toArray((V[])Array.newInstance((Class)valueType, result.size()));
            }
            else
            {
                throw new IllegalStateException("Array-conversion failed: Cannot convert not a collection to an array.");
            }
        }
        catch (ConversionException ex)
        {
            throw new IllegalStateException("Array-conversion failed: Error while converting the values in the array.");
        }
    }
}
