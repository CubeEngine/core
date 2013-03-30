package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentConverter implements Converter<Enchantment>
{
    @Override
    public Node toNode(Enchantment object) throws ConversionException
    {
        return Convert.wrapIntoNode(Match.enchant().nameFor(object));
    }

    @Override
    public Enchantment fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Match.enchant().enchantment(((StringNode)node).getValue());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
