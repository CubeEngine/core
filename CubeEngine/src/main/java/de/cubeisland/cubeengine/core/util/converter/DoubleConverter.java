package de.cubeisland.cubeengine.core.util.converter;

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