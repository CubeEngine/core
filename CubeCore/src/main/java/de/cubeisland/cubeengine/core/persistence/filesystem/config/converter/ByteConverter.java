package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

/**
 *
 * @author Faithcaio
 */
public class ByteConverter implements IConverter<Byte>
{
    public Object from(Byte object)
    {
        return object;
    }

    public Byte to(Object configElem)
    {
        return ((Integer) configElem).byteValue();
    }
}