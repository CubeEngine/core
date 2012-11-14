package de.cubeisland.cubeengine.core.util.converter;

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
    public Level fromObject(Object object) throws ConversionException
    {
        return LogLevel.parse(object.toString());
    }

    @Override
    public String toString(Level object)
    {
        return object.toString();
    }

    @Override
    public Level fromString(String string) throws ConversionException
    {
        return LogLevel.parse(string);
    }
}