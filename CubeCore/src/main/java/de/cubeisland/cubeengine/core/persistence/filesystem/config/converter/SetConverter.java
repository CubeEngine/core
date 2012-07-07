package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Anselm
 */
public class SetConverter implements Converter<Set>
{
    public Object from(Set object)
    {
        return object;
    }

    public Set to(Object object)
    {
        if (object instanceof Collection)
        {
            Set set = new HashSet((Collection)object);
            return set;
        }
        return null;
    }
}
