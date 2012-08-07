package de.cubeisland.cubeengine.core.user.event;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.persistence.event.ModelEvent;
import de.cubeisland.cubeengine.core.user.User;

/**
 *
 * @author Anselm Brehme
 */
public abstract class UserEvent extends ModelEvent
{
    private final User user;

    public UserEvent(Core core, User user)
    {
        super(core, user);
        this.user = user;
    }

    public User getUser()
    {
        return this.user;
    }
}