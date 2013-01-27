package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import org.bukkit.Material;

public class BlockDataConverter implements Converter<BlockData>
{
    @Override
    public Node toNode(BlockData object) throws ConversionException
    {
        if (object.data == 0)
        {
            return Convert.wrapIntoNode(String.valueOf(object.mat.getId()));
        }
        return Convert.wrapIntoNode(object.mat.getId() + ":" + object.data);
    }

    @Override
    public BlockData fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return fromString(node.unwrap());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public BlockData fromString(String string) throws ConversionException
    {
        if (string == null)
        {
            return null;
        }
        Material mat;
        Byte data;
        if (!string.contains(":"))
        {
            mat = Material.matchMaterial(string);
            data = 0;
        }
        else
        {
            mat = Material.matchMaterial(string.substring(0, string.indexOf(":")));
            data = Byte.parseByte(string.substring(string.indexOf(":")));
        }
        if (mat == null)
        {
            return null;
        }
        return new BlockData(mat, data);
    }
}
