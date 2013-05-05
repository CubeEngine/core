package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.clipboard.CuboidBlockClipboard;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class CuboidBlockClipboardConverter implements Converter<CuboidBlockClipboard>
{
    @Override
    public Node toNode(CuboidBlockClipboard object) throws ConversionException
    {
        return object.toNode();
    }

    @Override
    public CuboidBlockClipboard fromNode(Node node) throws ConversionException
    {
        return CuboidBlockClipboard.fromNode(node);
    }
}
