package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Convert;
import de.cubeisland.cubeengine.core.util.converter.Converter;
import java.util.Collection;
import java.util.LinkedList;

public class CollectionConverter
{
    /**
     * Returns the converted collection
     * 
     * @param col the collection to convert
     * @return the converted collection
     * @throws ConversionException 
     */
    public Object toObject(Collection col) throws ConversionException
    {
        Collection<Object> result = new LinkedList<Object>();
        if (col.isEmpty())
        {
            return result;
        }
        Converter valConverter = Convert.matchConverter(col.iterator().next().getClass());
        if (valConverter == null)
        {
            throw new IllegalStateException("Converter not found for: " + col.iterator().next().getClass().getCanonicalName());
        }
        for (Object value : col)
        {
            result.add(valConverter.toObject(value));
        }
        return result;
    }

    /**
     * Deserializes an object back to a collection
     * 
     * @param <V> the ValueType
     * @param object the object to convert
     * @param valueType the ValueTypeClass 
     * @return the converted collection
     * @throws ConversionException 
     */
    public <V> Collection<V> fromObject(Object object, Class<V> valueType) throws ConversionException
    {
        Collection<V> result = new LinkedList<V>();
        if (object instanceof Collection)
        {
            Converter<V> valConverter = Convert.matchConverter(valueType);
            Collection col = (Collection)object;
            for (Object o : col)
            {
                result.add(valConverter.fromObject(o));
            }
            return result;
        }
        else
        {
            throw new IllegalStateException("Cannot convert not a collection to a collection.");
        }
    }
}