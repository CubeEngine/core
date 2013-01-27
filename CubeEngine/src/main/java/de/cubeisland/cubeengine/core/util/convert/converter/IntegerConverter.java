package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.IntNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
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
        else if (node instanceof StringNode)
        {
            String s = ((StringNode) node).getValue();
            try
            {
                return Integer.parseInt(s.toString());
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException(e);
            }
        }
        throw  new ConversionException("Invalid Node!"+ node.getClass());
    }
}
