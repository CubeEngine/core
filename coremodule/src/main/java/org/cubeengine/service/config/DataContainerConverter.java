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
package org.cubeengine.service.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.converter.converter.ClassedConverter;
import de.cubeisland.engine.converter.node.Node;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;

public class DataContainerConverter implements ClassedConverter<DataContainer>
{
    @Override
    public Node toNode(DataContainer object, ConverterManager manager) throws ConversionException
    {
        Map<DataQuery, Object> deep = object.getValues(true);
        Map<String, Object> map = deep.entrySet().stream()
                                      .filter(e -> !(e.getValue() instanceof DataView))
                                      .collect(Collectors.toMap(e -> e.getKey().asString("_"), Entry::getValue));

        return manager.convertToNode(map);
    }

    @Override
    public DataContainer fromNode(Node node, Class<? extends DataContainer> type, ConverterManager manager) throws ConversionException
    {
        MemoryDataContainer data = new MemoryDataContainer();
        Map<String, Object> map = manager.convertFromNode(node, Map.class);
        map.entrySet().forEach(e -> data.set(DataQuery.of("_", e.getKey()), e.getValue()));
        return data;
    }
}
