package de.cubeisland.cubeengine.core.util.converter;

public class DoubleConverter extends BasicConverter<Double>
{
    @Override
    public Double fromString(String string) throws ConversionException
    {
        try
        {
            return Double.parseDouble(string);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}