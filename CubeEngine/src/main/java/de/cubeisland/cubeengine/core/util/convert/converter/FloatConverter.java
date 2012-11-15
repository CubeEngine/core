package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class FloatConverter extends BasicConverter<Float>
{
    @Override
    public Float fromObject(Object object) throws ConversionException
    {
        try
        {
            return Float.parseFloat(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
