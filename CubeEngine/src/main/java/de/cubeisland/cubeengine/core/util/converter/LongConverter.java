package de.cubeisland.cubeengine.core.util.converter;

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