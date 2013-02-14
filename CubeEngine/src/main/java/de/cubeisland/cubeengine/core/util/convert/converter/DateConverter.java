package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.sql.Date;

public class DateConverter implements Converter<Date>
{
    @Override
    public Node toNode(Date object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.toString());
    }

    @Override
    public Date fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Date.valueOf(((StringNode)node).getValue());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
