package de.cubeisland.cubeengine.core.util.converter;

public class LongConverter extends BasicConverter<Long>
{
    @Override
    public Long fromString(String string) throws ConversionException
    {
        try
        {
            return Long.parseLong(string);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}