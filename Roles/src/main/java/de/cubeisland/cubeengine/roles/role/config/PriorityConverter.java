package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class PriorityConverter implements Converter<Priority>
{
    @Override
    public Object toObject(Priority object) throws ConversionException
    {
        return object.toString();
    }

    @Override
    public Priority fromObject(Object object) throws ConversionException
    {
        Priority prio = Priority.getByName(object.toString());
        if (prio == null)
        {
            prio = new Priority(Integer.valueOf(object.toString()));
        }
        return prio;
    }
}
