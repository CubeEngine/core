package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class BasicConverter<T> implements Converter<T>
{
    public Object toObject(T object)
    {
        return object;
    }

    public T fromObject(Object object) throws ConversionException
    {
        return this.fromString(object.toString());
    }

    public String toString(T object)
    {
        return String.valueOf(object);
    }
}