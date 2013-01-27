package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.BasicConverter;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;

public class StringConverter extends BasicConverter<String>
{
    @Override
    public String fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return ((StringNode) node).getValue();
        }
        return node.unwrap();
    }
}
