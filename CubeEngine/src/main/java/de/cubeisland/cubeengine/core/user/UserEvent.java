package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.CubeEvent;

/**
 * This is a base for user related events.
 */
public abstract class UserEvent extends CubeEvent
{
    private final User user;

    public UserEvent(Core core, User user)
    {
        super(core);
        this.user = user;
    }

    /**
     * Returns the user corresponding to this event
     *
     * @return a user
     */
    public User getUser()
    {
        return this.user;
    }
}
