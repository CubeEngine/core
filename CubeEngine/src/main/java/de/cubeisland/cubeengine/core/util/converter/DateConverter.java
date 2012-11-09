package de.cubeisland.cubeengine.core.util.converter;

import java.sql.Date;

class DateConverter implements Converter<Date>
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