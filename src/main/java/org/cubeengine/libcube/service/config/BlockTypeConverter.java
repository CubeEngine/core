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
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.RegistryTypes;

public class BlockTypeConverter extends SimpleConverter<BlockType>
{

    @Override
    public Node toNode(BlockType object)
    {
        return StringNode.of(object.key(RegistryTypes.BLOCK_TYPE).asString());
    }

    @Override
    public BlockType fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return RegistryTypes.BLOCK_TYPE.get().findValue(ResourceKey.resolve(node.asText())).orElse(null);
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
