package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.config.node.StringNode;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class UserConverter implements Converter<User>
{
    @Override
    public Node toNode(User user) throws ConversionException
    {
        return Convert.wrapIntoNode(user.getName());
    }

    @Override
    public User fromNode(Node node) throws ConversionException
    {
        if (node instanceof StringNode)
        {
            return CubeEngine.getUserManager().findUser(((StringNode)node).getValue());
        }
        throw new ConversionException("Invalid Node!" + node.getClass());
    }
}
