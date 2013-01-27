package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.DoubleNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class DoubleConverter extends BasicConverter<Double>
{
    @Override
    public Double fromNode(Node node) throws ConversionException
    {
        if (node instanceof DoubleNode)
        {
            return ((DoubleNode)node).getValue();
        }
        String s = node.unwrap();
        try
        {
            return Double.parseDouble(s);
        }
        catch (NumberFormatException e)
        {
            throw  new ConversionException("Invalid Node!"+ node.getClass(), e);
        }
    }
}
