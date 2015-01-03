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

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;

public class WorldConverter extends SimpleConverter<World>
{
    @Override
    public Node toNode(World object) throws ConversionException
    {
        return StringNode.of(object.getName() + "(" + object.getUID().toString() + ")");
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
            throw ConversionException.of(this, node, "World not found! ");
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
