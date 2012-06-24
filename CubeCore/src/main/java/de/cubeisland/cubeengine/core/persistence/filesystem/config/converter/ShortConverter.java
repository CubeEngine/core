package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import java.util.List;

/**
 *
 * @author Faithcaio
 */
public class ShortConverter implements Converter<Short>
{
    public Object from(Short object)
    {
        return object;
    }

    public Short to(Object object)
    {
        return ((Integer) object).shortValue();
    }
}
