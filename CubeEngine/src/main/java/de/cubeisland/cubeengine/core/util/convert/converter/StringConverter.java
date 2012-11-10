package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class StringConverter implements Converter<String>
{
    @Override
    public Object toObject(String object) throws ConversionException
    {
        return object;
    }

    @Override
    public String fromObject(Object object) throws ConversionException
    {
        return object.toString();
    }
}