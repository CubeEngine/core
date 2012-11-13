package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.Pair;

/**
 * This argument is used to get users
 */
public class UserArg extends ArgumentReader<User>
{
    private final UserManager um;

    public UserArg()
    {
        super(User.class);
        this.um = CubeEngine.getUserManager();
    }

    @Override
    public Pair<Integer, User> read(String... args) throws InvalidArgumentException
    {
        User value = this.um.getUser(args[0], false);
        return new Pair<Integer, User>(value == null ? 0 : 1, value);
    }
}
