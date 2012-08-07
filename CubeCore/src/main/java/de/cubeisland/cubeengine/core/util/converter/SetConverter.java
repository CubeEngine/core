package de.cubeisland.cubeengine.core.util.converter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Anselm
 */
public class SetConverter implements Converter<Set<?>>
{
    public Object toObject(Set<?> object)
    {
        return object;
    }

    public Set<?> fromObject(Object object)
    {
        if (object instanceof Collection)
        {
            return new HashSet((Collection<?>)object);
        }
        return null;
    }

    public Set<?> fromString(String string)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toString(Set<?> object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
