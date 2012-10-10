package de.cubeisland.cubeengine.core.util.converter;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class ArrayConverter implements GenericConverter<Object[]>
{
    @Override
    public Object toObject(Object[] object, Class<?> genericType, String basepath) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        Object[] array = (Object[])object;
        if (converter != null)
        {
            Collection<Object> result = new LinkedList<Object>();
            for (Object o : array)
            {
                result.add(converter.toObject(o));
            }
            return result;
        }
        Collection<Object> result = new LinkedList<Object>();
        result.addAll(Arrays.asList(array));
        return result;

    }

    @Override
    public <G> Object[] fromObject(Object object, Object fieldObject, Class<G> genericType) throws ConversionException
    {
        Converter converter = Convert.matchConverter(genericType);
        Collection<Object> coll = (Collection)object;
        Object tmparray = coll.toArray();
        if (converter != null)
        {
            Object o = Array.newInstance(genericType, coll.size());
            for (int i = 0; i < coll.size(); ++i)
            {
                Array.set(o, i, converter.fromObject(Array.get(tmparray, i)));
            }
            return (G[])o;
        }
        Object o = Array.newInstance(genericType, coll.size());
        for (int i = 0; i < coll.size(); ++i)
        {
            Array.set(o, i, Array.get(tmparray, i));
        }
        return (G[])o;
    }
}
