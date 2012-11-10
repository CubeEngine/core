package de.cubeisland.cubeengine.core.util.convert;

import java.util.Collection;
import java.util.Map;

public abstract class BasicConverter<T> implements Converter<T>
{
    @Override
    public Object toObject(T object) throws ConversionException
    {
        Class<T> clazz = (Class<T>)object.getClass();
        if (clazz.isPrimitive()
            || Number.class.isAssignableFrom(clazz)
            || CharSequence.class.isAssignableFrom(clazz)
            || Boolean.class.isAssignableFrom(clazz)
            || Map.class.isAssignableFrom(clazz)
            || Collection.class.isAssignableFrom(clazz)
            || clazz.isArray()
        )
        {
            return object;
        }
        throw new ConversionException("Illegal object type");
    }
}