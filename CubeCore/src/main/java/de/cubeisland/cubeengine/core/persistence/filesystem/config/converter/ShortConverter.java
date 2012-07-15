package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.persistence.filesystem.config.Converter;

/**
 *
 * @author Anselm Brehme
 */
public class ShortConverter implements Converter<Short>
{
    public Object from(Short object)
    {
        return object;
    }

    public Short to(Object object)
    {
       Double t = Double.parseDouble(object.toString());
       return t.shortValue();
    }
}
