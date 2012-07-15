package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Converter;

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