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

public class WorldTransformConverter extends SingleClassConverter<WorldTransform>
{
    @Override
    public Node toNode(WorldTransform transform, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("x", transform.x);
        loc.put("y", transform.y);
        loc.put("z", transform.z);
        loc.put("rx", transform.rx);
        loc.put("ry", transform.ry);
        loc.put("rz", transform.rz);
        return manager.convertToNode(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WorldTransform fromNode(Node node, ConverterManager manager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            Map<String, Node> input = ((MapNode)node).getValue();
            double x = manager.convertFromNode(input.get("x"), double.class);
            double y = manager.convertFromNode(input.get("y"), double.class);
            double z = manager.convertFromNode(input.get("z"), double.class);
            double rx = manager.convertFromNode(input.get("rx"), double.class);
            double ry = manager.convertFromNode(input.get("ry"), double.class);
            double rz = manager.convertFromNode(input.get("rz"), double.class);
            return new WorldTransform(x, y, z, rx, ry, rz);
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");
    }
}
