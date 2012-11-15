package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.util.logging.Level;

public class LogLevelConverter implements Converter<Level>
{
    @Override
    public Object toObject(Level object) throws ConversionException
    {
        return object.toString();
    }

    @Override
    public LogLevel fromObject(Object object) throws ConversionException
    {
        LogLevel lv = LogLevel.parse(object.toString());
        if (lv == null)
        {
            throw new ConversionException("Unknown LogLevel. " + object.toString());
        }
        return lv;
    }
}
