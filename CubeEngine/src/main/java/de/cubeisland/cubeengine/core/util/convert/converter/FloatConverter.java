package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.FloatNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class FloatConverter extends BasicConverter<Float>
{
    @Override
    public Float fromNode(Node node) throws ConversionException
    {
        if (node instanceof FloatNode)
        {
            return ((FloatNode)node).getValue();
        }
        else if (node instanceof StringNode)
        {
            String s = ((StringNode) node).getValue();
            try
            {
                return Float.parseFloat(s.toString());
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException(e);
            }
        }
        throw  new ConversionException("Invalid Node!"+ node.getClass());
    }
}
