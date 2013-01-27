package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.ByteNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class ByteConverter extends BasicConverter<Byte>
{
    @Override
    public Byte fromNode(Node node) throws ConversionException
    {
        if (node instanceof ByteNode)
        {
            return ((ByteNode)node).getValue();
        }
        else if (node instanceof StringNode)
        {
            String s = ((StringNode) node).getValue();
            try
            {
                return Byte.parseByte(s.toString());
            }
            catch (NumberFormatException e)
            {
                throw new ConversionException(e);
            }
        }
        throw  new ConversionException("Invalid Node!"+ node.getClass());
    }
}
