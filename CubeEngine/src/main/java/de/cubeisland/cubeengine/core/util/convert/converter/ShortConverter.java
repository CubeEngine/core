package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class ShortConverter extends BasicConverter<Short>
{
    @Override
    public Short fromObject(Object object) throws ConversionException
    {
        try
        {
            return Short.parseShort(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
