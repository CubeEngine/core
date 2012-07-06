package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.event.UserEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Faithcaio
 */
public class FlyStartEvent extends UserEvent implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();

    public FlyStartEvent(CubeCore core, User user)
    {
        super(core, user);
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}