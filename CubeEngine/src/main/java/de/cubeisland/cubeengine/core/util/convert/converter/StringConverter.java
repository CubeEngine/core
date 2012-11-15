package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class StringConverter extends BasicConverter<String>
{
    @Override
    public String fromObject(Object object) throws ConversionException
    {
        return object.toString();
    }
}
