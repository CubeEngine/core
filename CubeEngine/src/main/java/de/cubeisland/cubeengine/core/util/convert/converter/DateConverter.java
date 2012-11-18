package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.sql.Date;

public class DateConverter implements Converter<Date>
{
    @Override
    public Object toObject(Date object) throws ConversionException
    {
        return object;
    }

    @Override
    public Date fromObject(Object object) throws ConversionException
    {
        return Date.valueOf(object.toString());
    }
}
