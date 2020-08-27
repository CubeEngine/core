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
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public class WorldConverter extends SimpleConverter<ServerWorld>
{
    @Override
    public Node toNode(ServerWorld object) throws ConversionException
    {
        return StringNode.of(object.getKey().asString());
    }

    @Override
    public ServerWorld fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            String string = ((StringNode)node).getValue();
            Optional<ServerWorld> world = Optional.empty();
            if (string.contains("(") && string.contains(")"))
            {
                UUID uid = UUID.fromString(string.substring(string.indexOf('(') + 1, string.indexOf(')')));
                world = Sponge.getServer().getWorldManager().getWorld(ResourceKey.sponge(uid.toString()));
                string = string.substring(0, string.indexOf('('));
            }
            if (!world.isPresent())
            {
                world = Sponge.getServer().getWorldManager().getWorld(ResourceKey.sponge(string));
            }
            if (world.isPresent())
            {
                return world.get();
            }
            throw ConversionException.of(this, node, "World not found! ");
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
