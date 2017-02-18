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

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.ConverterManager;
import org.cubeengine.converter.converter.ClassedConverter;
import org.cubeengine.converter.node.ListNode;
import org.cubeengine.converter.node.MapNode;
import org.cubeengine.converter.node.Node;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class DataContainerConverter implements ClassedConverter<DataContainer>
{
    @Override
    public Node toNode(DataContainer object, ConverterManager manager) throws ConversionException
    {
        return manager.convertToNode(object.getValues(false));
    }

    @Override
    public DataContainer fromNode(Node node, Class<? extends DataContainer> type, ConverterManager manager) throws ConversionException
    {
        MemoryDataContainer data = new MemoryDataContainer();
        for (Entry<String, Node> entry : ((MapNode) node).getValue().entrySet())
        {
            DataQuery key = DataQuery.of('_', ((MapNode) node).getOriginalKey(entry.getKey()));
            Object value;
            if (entry.getValue() instanceof ListNode)
            {
                value = toList(((ListNode) entry.getValue()));
            }
            else
            {
                Type vType = entry.getValue() instanceof MapNode ? DataContainer.class : entry.getValue().getValue().getClass();
                value = manager.convertFromNode(entry.getValue(), vType);
            }
            data.set(key, value);
        }
        return data;
    }

    private List toList(ListNode value)
    {
        List<Object> list = new ArrayList<>();
        for (Node node : value.getValue())
        {
            list.add(node.getValue());
        }
        return list;
    }
}
