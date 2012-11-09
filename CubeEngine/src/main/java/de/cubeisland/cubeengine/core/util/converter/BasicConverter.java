package de.cubeisland.cubeengine.core.util.converter;

public abstract class BasicConverter<T> implements Converter<T>
{
    @Override
    public Object toObject(T object)
    {
        return object;
    }
}