package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public class ShortConverter extends BasicConverter<Short>
{
    @Override
    public Short fromString(String string) throws ConversionException
    {
        try
        {
            return Short.parseShort(string);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}