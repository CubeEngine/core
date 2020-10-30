/*
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

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.ConverterManager;
import org.cubeengine.converter.converter.SingleClassConverter;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.spongepowered.api.util.Transform;
import org.spongepowered.math.vector.Vector3d;

import java.util.LinkedHashMap;
import java.util.Map;

public class TransformConverter extends SingleClassConverter<Transform>
{
    @Override
    public Node toNode(Transform transform, ConverterManager manager) throws ConversionException
    {
        Map<String, Object> loc = new LinkedHashMap<>();
        loc.put("x", transform.getPosition().getX());
        loc.put("y", transform.getPosition().getY());
        loc.put("z", transform.getPosition().getZ());
        loc.put("rx", transform.getRotation().getX());
        loc.put("ry", transform.getRotation().getY());
        loc.put("rz", transform.getRotation().getZ());
        return manager.convertToNode(loc);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Transform fromNode(Node node, ConverterManager manager) throws ConversionException
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
            return Transform.of(new Vector3d(x, y, z), new Vector3d(rx, ry, rz));
        }
        throw ConversionException.of(this, node, "Node is not a MapNode!");
    }
}
