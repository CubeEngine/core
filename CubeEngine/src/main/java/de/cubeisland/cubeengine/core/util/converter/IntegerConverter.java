package de.cubeisland.cubeengine.core.util.converter;

public class IntegerConverter extends BasicConverter<Integer>
{
    @Override
    public Integer fromObject(Object object) throws ConversionException
    {
        try
        {
            return Integer.parseInt(object.toString());
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}