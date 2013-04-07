package de.cubeisland.cubeengine.log.action.logaction.container;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class ContainerTypeConverter implements Converter<ContainerType>
{
    @Override
    public Node toNode(ContainerType object) throws ConversionException
    {
        return new StringNode(object.name);
    }

    @Override
    public ContainerType fromNode(Node node) throws ConversionException
    {
        return ContainerType.ofName(node.unwrap());
    }
}
