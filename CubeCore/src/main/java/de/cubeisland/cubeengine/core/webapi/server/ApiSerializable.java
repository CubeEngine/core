package de.cubeisland.cubeengine.core.webapi.server;

/**
 * Classes which implement this interface can be directly passed to the
 * ApiResponse
 *
 * @author Phillip Schichtel
 * @since 1.0.0
 */
public interface ApiSerializable
{
    /**
     * This method serialzes the object into a structure of Maps, Lists and
     * primitive types like numbers and booleans
     *
     * @return the serialized object
     */
    public Object serialize();
}