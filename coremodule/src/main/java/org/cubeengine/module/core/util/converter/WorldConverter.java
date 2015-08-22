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
package org.cubeengine.module.core.util.converter;

import java.util.UUID;
import com.google.common.base.Optional;
import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;
import org.spongepowered.api.Server;
import org.spongepowered.api.world.World;

public class WorldConverter extends SimpleConverter<World>
{
    private final Server server;

    public WorldConverter(Server server)
    {
        this.server = server;
    }

    @Override
    public Node toNode(World object) throws ConversionException
    {
        return StringNode.of(object.getName() + "(" + object.getUniqueId().toString() + ")");
    }

    @Override
    public World fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            String string = ((StringNode)node).getValue();
            Optional<World> world = Optional.absent();
            if (string.contains("(") && string.contains(")"))
            {
                UUID uid = UUID.fromString(string.substring(string.indexOf('(') + 1, string.indexOf(')')));
                world = server.getWorld(uid);
                string = string.substring(0, string.indexOf('('));
            }
            if (!world.isPresent())
            {
                world = server.getWorld(string);
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
