package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class ByteConverter extends BasicConverter<Byte>
{
    @Override
    public Byte fromObject(Object object) throws ConversionException
    {
        try
        {
            return Byte.parseByte(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
