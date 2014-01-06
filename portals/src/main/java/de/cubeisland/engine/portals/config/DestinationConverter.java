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
package de.cubeisland.engine.portals.config;

import java.util.LinkedHashMap;

import de.cubeisland.engine.configuration.codec.ConverterManager;
import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.MapNode;
import de.cubeisland.engine.configuration.node.Node;
import de.cubeisland.engine.configuration.node.StringNode;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.portals.config.Destination;
import de.cubeisland.engine.portals.config.Destination.Type;

public class DestinationConverter implements Converter<Destination>
{
    public DestinationConverter(Core core)
    {
        this.core = core;
    }

    private final Core core;

    @Override
    public Node toNode(Destination destination, ConverterManager converterManager) throws ConversionException
    {
        MapNode result = MapNode.emptyMap();
        result.setExactNode("type", StringNode.of(destination.type.name()));
        switch (destination.type)
        {
        case PORTAL:
            result.setExactNode("portal", StringNode.of(destination.portal));
            break;
        case WORLD:
            result.setExactNode("world", StringNode.of(destination.world.getName()));
            break;
        case LOCATION:
            result.setExactNode("world", StringNode.of(destination.world.getName()));
            result.setExactNode("location", converterManager.convertToNode(destination.location));
            break;
        }
        return result;
    }

    @Override
    public Destination fromNode(Node node, ConverterManager converterManager) throws ConversionException
    {
        if (node instanceof MapNode)
        {
            LinkedHashMap<String,Node> mappedNodes = ((MapNode)node).getMappedNodes();
            try
            {
                Type type = Type.valueOf(mappedNodes.get("type").asText());
                Destination destination = new Destination();
                destination.type = type;
                switch (type)
                {
                case PORTAL:
                    destination.portal = mappedNodes.get("portal").asText();
                    break;
                case WORLD:
                    destination.world = core.getWorldManager().getWorld(mappedNodes.get("world").asText());
                    break;
                case LOCATION:
                    destination.world = core.getWorldManager().getWorld(mappedNodes.get("world").asText());
                    destination.location = converterManager.convertFromNode(mappedNodes.get("location"), WorldLocation.class);
                    break;
                }
                return destination;
            }
            catch (IllegalArgumentException e)
            {
                throw ConversionException.of(this, mappedNodes.get("type"), "Could not read Type!");
            }
        }
        throw ConversionException.of(this, node, "Node is not a mapnode!");
    }
}
