package de.cubeisland.cubeengine.core.util.converter;

public abstract class BasicConverter<T> implements Converter<T>
{
    @Override
    public Object toObject(T object)
    {
        return object;
    }

    @Override
    public T fromObject(Object object) throws ConversionException
    {
        return this.fromString(object.toString());
    }

    @Override
    public String toString(T object)
    {
        return String.valueOf(object);
    }
}