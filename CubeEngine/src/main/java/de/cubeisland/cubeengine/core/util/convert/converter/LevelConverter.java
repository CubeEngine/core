package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
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
}