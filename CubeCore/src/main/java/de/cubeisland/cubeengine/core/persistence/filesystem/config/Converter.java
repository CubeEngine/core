package de.cubeisland.cubeengine.core.persistence.filesystem.config;

/**
 *
 * @author Phillip Schichtel
 */
public interface Converter<T>
{
    /**
     * Converts a field value into serializable objects
     * 
     * @param object the fieldvalue
     * @return the serialized fieldvalue
     */
    public Object from(T object);

    /**
     * Converts a serialized object back into the field
     * 
     * @param object the object to deserialize
     * @return the deserialized fieldvalue
     */
    public T to(Object object);
}