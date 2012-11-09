package de.cubeisland.cubeengine.core.util.converter;

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