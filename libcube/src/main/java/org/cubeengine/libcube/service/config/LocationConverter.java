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
package org.cubeengine.libcube.service.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.ConverterManager;
import org.cubeengine.converter.converter.SingleClassConverter;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LocationConverter extends SingleClassConverter<Location<World>>
{
    @Override
    public Node toNode(Location<World> location, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("world", location.getExtent().getName());
        loc.put("x", location.getX());
        loc.put("y", location.getY());
        loc.put("z", location.getZ());
        //loc.put("yaw", location.getYaw()); // TODO Location + Direction
        //loc.put("pitch", location.getPitch()); // TODO Location + Direction
        return manager.convertToNode(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Location<World> fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Map<String, Node> input = ((MapNode)node).getValue();
            World world = Sponge.getServer().getWorld(((StringNode)input.get("world")).getValue()).get();
            double x = manager.convertFromNode(input.get("x"), double.class);
            double y = manager.convertFromNode(input.get("y"), double.class);
            double z = manager.convertFromNode(input.get("z"), double.class);
            //double yaw = manager.convertFromNode(input.get("yaw"), double.class);
            //double pitch = manager.convertFromNode(input.get("pitch"), double.class);
            return new Location(world, x, y, z); // TODO Location + Direction
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");
    }
}
