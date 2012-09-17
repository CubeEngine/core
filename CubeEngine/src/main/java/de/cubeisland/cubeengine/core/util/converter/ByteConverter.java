package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public class ByteConverter extends BasicConverter<Byte>
{
    @Override
    public Byte fromString(String string) throws ConversionException
    {
        try
        {
            return Byte.parseByte(string);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}