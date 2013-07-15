/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.core.util.convert.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.config.node.MapNode;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

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
