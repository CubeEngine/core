package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Pair;

/**
 * This argument is used to get users
 */
public class UserReader extends ArgumentReader<User>
{
    private final UserManager um;

    public UserReader()
    {
        super(User.class);
        this.um = CubeEngine.getUserManager();
    }

    @Override
    public Pair<Integer, User> read(String... args) throws InvalidArgumentException
    {
        User value = this.um.findUser(args[0]);
        return new Pair<Integer, User>(0, value);
    }
}
