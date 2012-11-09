package de.cubeisland.cubeengine.core.util.converter;

public class IntegerConverter extends BasicConverter<Integer>
{
    @Override
    public Integer fromString(String string) throws ConversionException
    {
        try
        {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException ex)
        {
            throw new ConversionException(string + " is no Integer", ex);
        }
    }
}