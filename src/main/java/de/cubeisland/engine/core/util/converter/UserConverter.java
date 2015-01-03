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

import de.cubeisland.engine.converter.ConversionException;
import de.cubeisland.engine.converter.converter.SimpleConverter;
import de.cubeisland.engine.converter.node.Node;
import de.cubeisland.engine.converter.node.StringNode;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;

public class UserConverter extends SimpleConverter<User>
{
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
            User user = CubeEngine.getUserManager().findUser(((StringNode)node).getValue());
            if (user != null)
            {
                return user;
            }
            throw ConversionException.of(this, node, "User does not exist!");
        }
        throw ConversionException.of(this, node, "Node is not a StringNode!");
    }
}
