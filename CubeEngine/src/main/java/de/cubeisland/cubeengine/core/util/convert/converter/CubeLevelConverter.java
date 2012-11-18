package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.log.CubeLevel;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.util.logging.Level;

public class CubeLevelConverter implements Converter<Level>
{
    @Override
    public Object toObject(Level object) throws ConversionException
    {
        return object.toString();
    }

    @Override
    public CubeLevel fromObject(Object object) throws ConversionException
    {
        CubeLevel lv = LogLevel.parse(object.toString());
        if (lv == null)
        {
            throw new ConversionException("Unknown LogLevel. " + object.toString());
        }
        return lv;
    }
}
