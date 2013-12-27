package de.cubeisland.engine.core.util.converter;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.IntNode;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.core.util.math.BlockVector3;

public class BlockVector3Converter implements Converter<BlockVector3>
{
    @Override
    public Node toNode(BlockVector3 blockVector3, ConverterManager converterManager) throws ConversionException
    {
        MapNode mapNode = MapNode.emptyMap();
        mapNode.setExactNode("x", new IntNode(blockVector3.x));
        mapNode.setExactNode("y", new IntNode(blockVector3.y));
        mapNode.setExactNode("z", new IntNode(blockVector3.z));
        return mapNode;
    }

    @Override
    public BlockVector3 fromNode(Node node, ConverterManager converterManager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Node x = ((MapNode)node).getExactNode("x");
            Node y = ((MapNode)node).getExactNode("y");
            Node z = ((MapNode)node).getExactNode("z");
            return new BlockVector3((Integer)converterManager.convertFromNode(x, Integer.class),
                                    (Integer)converterManager.convertFromNode(y, Integer.class),
                                    (Integer)converterManager.convertFromNode(z, Integer.class));
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");
    }
}
