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

import static org.cubeengine.libcube.util.ContextUtil.GLOBAL;
import static org.spongepowered.api.service.context.Context.WORLD_KEY;

import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.context.Context;

public class ContextConverter extends SimpleConverter<Context>
{
    @Override
    public Node toNode(Context object)
    {
        return StringNode.of(object.getValue().isEmpty() ? object.getKey() : object.getKey() + "|" + object.getValue());
    }

    @Override
    public Context fromNode(Node node) throws ConversionException
    {
        String token = node.asText();
        String checkToken = token.toLowerCase();
        if (GLOBAL.getKey().equalsIgnoreCase(token))
        {
            return GLOBAL;
        }
        if (token.contains("|"))
        {
            String[] parts = token.split("\\|", 2);
            return new Context(parts[0], parts[1]);
        }
        if (isValidWorld(checkToken)) // try for world
        {
            return new Context(WORLD_KEY, checkToken);
        }
        throw ConversionException.of(this, node, "Unknown context: " + token);
    }

    private boolean isValidWorld(String token)
    {
        return Sponge.server().worldManager().worldKeys().contains(ResourceKey.sponge(token));
    }
}
