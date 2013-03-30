package de.cubeisland.cubeengine.core.user;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.event.HandlerList;

public class UserAuthorizedEvent extends UserEvent
{
    private static final HandlerList handlers = new HandlerList();

    public UserAuthorizedEvent(Core core, User user)
    {
        super(core, user);
    }

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

}
