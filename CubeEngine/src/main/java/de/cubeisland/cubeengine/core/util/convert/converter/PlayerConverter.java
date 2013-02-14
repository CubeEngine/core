package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class PlayerConverter implements Converter<OfflinePlayer>
{
    private Server server;

    public PlayerConverter(Core core)
    {
        this.server = ((Plugin)core).getServer();
    }

    @Override
    public Node toNode(OfflinePlayer object)
    {
        return Convert.wrapIntoNode(object.getName());
    }

    @Override
    public OfflinePlayer fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return this.server.getOfflinePlayer(((StringNode)node).getValue());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
