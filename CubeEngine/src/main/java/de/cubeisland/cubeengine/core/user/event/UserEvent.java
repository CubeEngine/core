package de.cubeisland.cubeengine.core.user.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEvent;
import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author Anselm Brehme
 */
public abstract class UserEvent extends CubeEvent
{
    private final User user;

    public UserEvent(Core core, User user)
    {
        super(core);
        this.user = user;
    }

    public User getUser()
    {
        return this.user;
    }
}