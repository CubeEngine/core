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

import java.util.Optional;
import org.cubeengine.converter.ConversionException;
import org.cubeengine.converter.converter.SimpleConverter;
import org.cubeengine.converter.node.Node;
import org.cubeengine.converter.node.StringNode;
import org.cubeengine.libcube.service.matcher.UserMatcher;
import org.spongepowered.api.entity.living.player.User;

public class UserConverter extends SimpleConverter<User>
{
    private UserMatcher um;

    public UserConverter(UserMatcher um)
    {
        this.um = um;
    }

    @Override
    public Node toNode(User user) throws ConversionException
    {
        return StringNode.of(user.getName());
    }

    @Override
    public User fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            Optional<User> user = um.match(((StringNode)node).getValue(), false);
            if (user.isPresent())
            {
                return user.get();
            }
            throw ConversionException.of(this, node, "User does not exist!");
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
