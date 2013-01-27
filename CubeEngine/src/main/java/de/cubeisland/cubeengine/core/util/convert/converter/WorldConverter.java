package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public class WorldConverter implements Converter<World>
{
    @Override
    public Node toNode(World object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.getName() + "(" + object.getUID().toString() + ")");
    }

    @Override
    public World fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            String string = ((StringNode) node).getValue();
            World world = null;
            if (string.contains("(") && string.contains(")"))
            {
                UUID uid = UUID.fromString(string.substring(string.indexOf('(') + 1, string.indexOf(')')));
                world = Bukkit.getWorld(uid);
                string = string.substring(0, string.indexOf('('));
            }
            if (world == null)
            {
                world = Bukkit.getWorld(string);
            }
            if (world != null)
            {
                return world;
            }
        }
        throw new ConversionException("Could not convert to User!");

    }
}
