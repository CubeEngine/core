package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class IntegerConverter extends BasicConverter<Integer>
{
    @Override
    public Integer fromNode(Node node) throws ConversionException
    {
        if (node instanceof IntNode)
        {
            return ((IntNode)node).getValue();
        }
        String s = node.unwrap();
        try
        {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException("Invalid Node!" + node.getClass(), e);
        }
    }
}
