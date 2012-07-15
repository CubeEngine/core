package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Converter;

/**
 *
 * @author Anselm Brehme
 */
public class ByteConverter implements Converter<Byte>
{
    public Object from(Byte object)
    {
        return object;
    }

    public Byte to(Object object)
    {
        Double t = Double.parseDouble(object.toString());
        return t.byteValue();
    }
}