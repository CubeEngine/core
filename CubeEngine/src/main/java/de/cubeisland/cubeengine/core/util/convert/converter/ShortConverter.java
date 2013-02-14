package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.ShortNode;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class ShortConverter extends BasicConverter<Short>
{
    @Override
    public Short fromNode(Node node) throws ConversionException
    {
        if (node instanceof ShortNode)
        {
            return ((ShortNode)node).getValue();
        }
        String s = node.unwrap();
        try
        {
            return Short.parseShort(s);
        }
        catch (NumberFormatException e)
        {
            throw new ConversionException("Invalid Node!" + node.getClass(), e);
        }
    }
}
