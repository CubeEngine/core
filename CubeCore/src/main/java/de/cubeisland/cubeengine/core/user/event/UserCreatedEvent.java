package de.cubeisland.cubeengine.core.user.event;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.HandlerList;

/**
 *
 * @author CubeIsland-Dev
 */
public class UserCreatedEvent extends UserEvent
{
    private static final HandlerList handlers = new HandlerList();

    public UserCreatedEvent(CubeCore core, User user) 
    {
        super(core, user);
    }
}
