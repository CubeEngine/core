package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public class FloatConverter extends BasicConverter<Float>
{
    @Override
    public Float fromString(String string) throws ConversionException
    {
        try
        {
            return Float.parseFloat(string);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException(e);
        }
    }
}
