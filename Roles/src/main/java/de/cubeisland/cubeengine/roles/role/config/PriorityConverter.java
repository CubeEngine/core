package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class PriorityConverter implements Converter<Priority>
{
    @Override
    public Node toNode(Priority object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.toString());
    }

    @Override
    public Priority fromNode(Node node) throws ConversionException
    {
        Priority prio = Priority.getByName(node.unwrap());
        if (prio == null)
        {
            prio = new Priority(Integer.valueOf(node.unwrap()));
        }
        return prio;
    }
}
