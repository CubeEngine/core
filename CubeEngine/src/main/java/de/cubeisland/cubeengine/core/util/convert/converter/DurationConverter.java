package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.time.Duration;

public class DurationConverter implements Converter<Duration>
{

    @Override
    public Object toObject(Duration object) throws ConversionException
    {
        return object.format();
    }

    @Override
    public Duration fromObject(Object object) throws ConversionException
    {
        return new Duration(object.toString());
    }
}
