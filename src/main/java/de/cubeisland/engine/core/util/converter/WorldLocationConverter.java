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
package de.cubeisland.engine.core.util.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.converter.converter.SingleClassConverter;
import de.cubeisland.engine.converter.node.MapNode;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.core.util.WorldLocation;

public class WorldLocationConverter extends SingleClassConverter<WorldLocation>
{
    @Override
    public Node toNode(WorldLocation location, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("x", location.x);
        loc.put("y", location.y);
        loc.put("z", location.z);
        loc.put("yaw", location.yaw);
        loc.put("pitch", location.pitch);
        return manager.convertToNode(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WorldLocation fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Map<String, Node> input = ((MapNode)node).getValue();
            double x = manager.convertFromNode(input.get("x"), double.class);
            double y = manager.convertFromNode(input.get("y"), double.class);
            double z = manager.convertFromNode(input.get("z"), double.class);
            float yaw = manager.convertFromNode(input.get("yaw"), float.class);
            float pitch = manager.convertFromNode(input.get("pitch"), float.class);
            return new WorldLocation(x, y, z, yaw, pitch);
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");
    }
}
