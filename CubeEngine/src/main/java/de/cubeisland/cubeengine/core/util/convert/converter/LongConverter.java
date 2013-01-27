package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.LongNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class LongConverter extends BasicConverter<Long>
{
    @Override
    public Long fromNode(Node node) throws ConversionException
    {
        if (node instanceof LongNode)
        {
            return ((LongNode)node).getValue();
        }
        String s = node.unwrap();
        try
        {
            return Long.parseLong(s);
        }
        catch (NumberFormatException e)
        {
            throw  new ConversionException("Invalid Node!"+ node.getClass(), e);
        }
    }
}
