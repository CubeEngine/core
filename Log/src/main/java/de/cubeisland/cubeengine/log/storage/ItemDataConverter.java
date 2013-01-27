package de.cubeisland.cubeengine.log.storage;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class ItemDataConverter implements Converter<ItemData>
{
    @Override
    public Node toNode(ItemData object) throws ConversionException
    {
        if (object.data == 0)
        {
            return Convert.wrapIntoNode(String.valueOf(object.mat));
        }
        return Convert.wrapIntoNode(object.mat + ":" + object.data);
    }

    @Override
    public ItemData fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return fromString(node.unwrap());
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }



    public ItemData fromString(String string) throws ConversionException
    {
        if (string == null)
        {
            return null;
        }
        int mat;
        Short data;
        if (string.contains(":"))
        {
            mat = Integer.parseInt(string.substring(0, string.indexOf(":")));
            data = 0;
        }
        else
        {
            mat = Integer.parseInt(string.substring(0, string.indexOf(":")));
            data = Short.parseShort(string.substring(string.indexOf(":")));
        }
        return new ItemData(mat, data);
    }
}
