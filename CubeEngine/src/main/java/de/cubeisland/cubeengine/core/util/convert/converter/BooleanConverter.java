package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class BooleanConverter implements Converter<Boolean>
{
    @Override
    public Object toObject(Boolean object) throws ConversionException
    {
        return object;
    }

    @Override
    public Boolean fromObject(Object object) throws ConversionException
    {
        String s = object.toString();
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("1"))
        {
            return true;
        }
        else
        {
            if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("off") || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("0"))
            {
                return false;
            }
            else
            {
                return null;
            }
        }
    }
}