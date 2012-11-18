package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class LongConverter extends BasicConverter<Long>
{
    @Override
    public Long fromObject(Object object) throws ConversionException
    {
        try
        {
            return Long.parseLong(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
