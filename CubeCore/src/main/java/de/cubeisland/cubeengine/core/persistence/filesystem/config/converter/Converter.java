package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

public interface Converter<T>
{
    public Object from(T object);

    public T to(Object object);
}