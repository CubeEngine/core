package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

/**
 *
 * @author Faithcaio
 */
public class ShortConverter implements IConverter<Short>
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
