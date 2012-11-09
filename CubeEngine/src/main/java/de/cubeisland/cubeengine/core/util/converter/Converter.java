package de.cubeisland.cubeengine.core.util.converter;

public interface Converter<T extends Object>
{
    /**
     * Converts this class to an serializable object
     *
     * @param object the fieldvalue
     * @return the serialized fieldvalue
     */
    public Object toObject(T object) throws ConversionException;

    /**
     * Converts the given object to this class
     *
     * @param object the object to deserialize
     * @return the deserialized fieldvalue
     */
    public T fromObject(Object object) throws ConversionException;

    public String toString(T object);

    /**
     * Converts the given String to this class
     *
     * @param object the object to deserialize
     * @return the deserialized fieldvalue
     */
    public T fromString(String string) throws ConversionException;
}