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
package de.cubeisland.engine.core.util.convert.converter;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.core.config.node.Node;
import de.cubeisland.engine.core.config.node.StringNode;
import de.cubeisland.engine.core.util.convert.ConversionException;
import de.cubeisland.engine.core.util.convert.Convert;
import de.cubeisland.engine.core.util.convert.Converter;

public class WorldConverter implements Converter<World>
{
    @Override
    public Node toNode(World object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.getName() + "(" + object.getUID().toString() + ")");
    }

    @Override
    public World fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            String string = ((StringNode)node).getValue();
            World world = null;
            if (string.contains("(") && string.contains(")"))
            {
                UUID uid = UUID.fromString(string.substring(string.indexOf('(') + 1, string.indexOf(')')));
                world = Bukkit.getWorld(uid);
                string = string.substring(0, string.indexOf('('));
            }
            if (world == null)
            {
                world = Bukkit.getWorld(string);
            }
            if (world != null)
            {
                return world;
            }
            throw new ConversionException("Could not convert to a world: World not found!");
        }
        throw new ConversionException("Could not convert to a world: The given node is not a string");
    }
}
