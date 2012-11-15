package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.event.HandlerList;

// TODO do we use this ????? We have Callbacks
/**
 * This Event is fired when a new User got created and added to the database.
 */
public class UserCreatedEvent extends UserEvent
{
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public UserCreatedEvent(Core core, User user)
    {
        super(core, user);
    }
}
