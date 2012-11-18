package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class DoubleConverter extends BasicConverter<Double>
{
    @Override
    public Double fromObject(Object object) throws ConversionException
    {
        try
        {
            return Double.parseDouble(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
