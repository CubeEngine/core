package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

/**
 *
 * @author Anselm Brehme
 */
public class DoubleConverter implements Converter<Double>
{
    public Object from(Double object)
    {
        return object;
    }

    public Double to(Object object)
    {
        return Double.parseDouble(object.toString());
    }
}