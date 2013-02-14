package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.logger.CubeLevel;
import de.cubeisland.cubeengine.core.logger.LogLevel;

public class CubeLevelConverter implements Converter<CubeLevel>
{
    @Override
    public Node toNode(CubeLevel object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.toString());
    }

    @Override
    public CubeLevel fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            CubeLevel lv = LogLevel.parse(((StringNode)node).getValue());
            if (lv == null)
            {
                throw new ConversionException("Unknown LogLevel. " + ((StringNode)node).getValue());
            }
            return lv;
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
