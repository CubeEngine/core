package de.cubeisland.cubeengine.core.util.convert;

public abstract class BasicConverter<T> implements Converter<T>
{
    @Override
    @SuppressWarnings("unchecked")
    public Object toObject(T object) throws ConversionException
    {
        Class<T> clazz = (Class<T>)object.getClass();
        if (clazz.isPrimitive()
            || Number.class.isAssignableFrom(clazz)
            || CharSequence.class.isAssignableFrom(clazz)
            || Boolean.class.isAssignableFrom(clazz))
        {
            return object;
        }
        throw new ConversionException("Illegal object type");
    }
}
