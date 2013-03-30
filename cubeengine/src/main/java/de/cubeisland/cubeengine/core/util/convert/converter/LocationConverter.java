package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocationConverter implements Converter<Location>
{
    private Server server;

    public LocationConverter(Core core)
    {
        this.server = ((Plugin)core).getServer();
    }

    @Override
    public Node toNode(Location location) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<String, Object>();
        loc.put("world", location.getWorld().getName());
        loc.put("x", location.getX());
        loc.put("y", location.getY());
        loc.put("z", location.getZ());
        loc.put("yaw", location.getYaw());
        loc.put("pitch", location.getPitch());
        return Convert.wrapIntoNode(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Location fromNode(Node node) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Map<String, Node> input = ((MapNode)node).getMappedNodes();
            World world = server.getWorld(((StringNode)input.get("world")).getValue());
            double x = Convert.fromNode(input.get("x"), double.class);
            double y = Convert.fromNode(input.get("y"), double.class);
            double z = Convert.fromNode(input.get("z"), double.class);
            double yaw = Convert.fromNode(input.get("yaw"), double.class);
            double pitch = Convert.fromNode(input.get("pitch"), double.class);

            return new Location(world, x, y, z, (float)yaw, (float)pitch);
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
