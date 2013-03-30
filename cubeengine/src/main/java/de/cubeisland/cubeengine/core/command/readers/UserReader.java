package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.user.User;

/**
 * This argument is used to get users
 */
public class UserReader extends ArgumentReader<User>
{
    public UserReader()
    {
        super(User.class);
    }

    @Override
    public User read(String arg) throws InvalidArgumentException
    {
        return CubeEngine.getUserManager().findUser(arg);
    }
}
