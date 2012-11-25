package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.log.CubeLevel;
import de.cubeisland.cubeengine.core.util.log.LogLevel;

public class CubeLevelConverter implements Converter<CubeLevel>
{
    @Override
    public Object toObject(CubeLevel object) throws ConversionException
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
