package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.converter.ConversionException;
import de.cubeisland.cubeengine.core.util.converter.Converter;
import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;

public abstract class ArraysConverter<T> implements Converter<T[]>
{
    protected Converter<T> converter;
    protected Class<T> clazz;

    @Override
    public Object toObject(T[] object) throws ConversionException
    {
        List<Object> list = new LinkedList<Object>();
        for (T t : object)
        {
            list.add(converter.toObject(t));
        }
        return list;
    }

    @Override
    public T[] fromObject(Object object) throws ConversionException
    {
        if (object instanceof List)
        {
            Object array = Array.newInstance(Object.class, ((List)object).size());
            int index = 0;
            for (Object o : (List)object)
            {
                Array.set(array, index, converter.fromObject(o));
                index++;
            }
            return (T[])array;
        }
        else
        {
            if (object.getClass().isArray())
            {
                Object array = Array.newInstance(clazz, ((Object[])object).length);
                int index = 0;
                for (Object o : (Object[])object)
                {
                    Array.set(array, index, converter.fromObject(o));
                    index++;
                }
                return (T[])array;
            }
            else
            {
                throw new ConversionException("Could not convert to an Array!");
            }
        }
    }

    @Override
    public String toString(T[] object)
    {
        List<String> strings = new LinkedList<String>();
        for (T t : object)
        {
            strings.add(converter.toString(t));
        }
        return StringUtils.implode(",", strings);
    }

    @Override
    public T[] fromString(String string) throws ConversionException
    {
        String[] strings = StringUtils.explode(",", string);
        List<T> list = new LinkedList<T>();
        for (String s : strings)
        {
            list.add(converter.fromString(s));
        }
        return (T[])list.toArray();
    }
}