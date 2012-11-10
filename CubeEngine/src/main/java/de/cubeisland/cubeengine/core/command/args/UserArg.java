package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;

/**
 * This argument is used to get users
 */
public class UserArg extends AbstractArgument<User>
{
    private final UserManager um;
    
    public UserArg()
    {
        super(User.class);
        this.um = CubeEngine.getUserManager();
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        this.value = this.um.getUser(args[0], false);
        return this.value == null ? 0 : 1;
    }
}
