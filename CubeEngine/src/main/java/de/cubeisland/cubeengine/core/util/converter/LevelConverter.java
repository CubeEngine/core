package de.cubeisland.cubeengine.core.util.converter;

import java.util.logging.Level;

public class LevelConverter implements Converter<Level>
{
    @Override
    public Object toObject(Level object) throws ConversionException
    {
        return object.toString();
    }

    @Override
    public Level fromObject(Object object) throws ConversionException
    {
        return Level.parse(object.toString());
    }

    @Override
    public String toString(Level object)
    {
        return object.toString();
    }

    @Override
    public Level fromString(String string) throws ConversionException
    {
        return Level.parse(string);
    }
}