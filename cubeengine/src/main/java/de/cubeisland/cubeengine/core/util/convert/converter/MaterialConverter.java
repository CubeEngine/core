package de.cubeisland.cubeengine.core.util.convert.converter;

import org.bukkit.Material;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.Match;

public class MaterialConverter implements Converter<Material>
{
    @Override
    public Node toNode(Material object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.name());
    }

    @Override
    public Material fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return Match.material().material(node.unwrap());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
