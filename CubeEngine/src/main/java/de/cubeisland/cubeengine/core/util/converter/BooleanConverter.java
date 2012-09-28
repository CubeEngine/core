package de.cubeisland.cubeengine.core.util.converter;

/**
 *
 * @author Anselm Brehme
 */
public class BooleanConverter implements Converter<Boolean>
{
    @Override
    public Object toObject(Boolean object) throws ConversionException
    {
        if (object)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Boolean fromObject(Object object) throws ConversionException
    {
        return this.fromString(object.toString());
    }

    @Override
    public String toString(Boolean object)
    {
        return object.toString();
    }

    @Override
    public Boolean fromString(String s) throws ConversionException
    {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("1"))
        {
            return true;
        }
        else if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("0"))
        {
            return false;
        }
        else
        {
            return null;
        }
    }
}
