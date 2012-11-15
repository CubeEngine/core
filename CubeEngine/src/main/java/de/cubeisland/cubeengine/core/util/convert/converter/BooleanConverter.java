package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class BooleanConverter extends BasicConverter<Boolean>
{
    @Override
    public Boolean fromObject(Object object) throws ConversionException
    {
        String s = object.toString();
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