/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cubeisland.cubeengine.core.util.converter;

import java.sql.Date;

/**
 *
 * @author Anselm Brehme
 */
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
        return this.fromString(object.toString());
    }

    @Override
    public String toString(Date object)
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Date fromString(String string) throws ConversionException
    {
        return Date.valueOf(string);
    }
    
}
