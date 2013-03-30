package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.inventory.ItemStack;

public class ItemStackConverter implements Converter<ItemStack>
{
    @Override
    public Node toNode(ItemStack object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.getType().getId() + ":" + object.getDurability());
    }

    @Override
    public ItemStack fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Match.material().itemStack(((StringNode)node).getValue());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
