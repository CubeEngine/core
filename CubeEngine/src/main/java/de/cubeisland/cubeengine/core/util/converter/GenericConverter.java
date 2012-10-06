package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public interface GenericConverter<T extends Object>
{
    /**
     * Converts this class to an serializable object
     *
     * @param object the fieldvalue
     * @param genericType the genricType of the object
     * @return the serialized fieldvalue
     */
    public Object toObject(T object, Class<?> genericType, String basepath) throws ConversionException;

    /**
     * Converts the given object to this class
     *
     * @param object the object to deserialize
     * @param genericType the genricType of the object
     * @return the deserialized fieldvalue
     */
    public <G> T fromObject(Object object, Object fieldObject, Class<G> genericType) throws ConversionException;
}
