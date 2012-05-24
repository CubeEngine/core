package de.cubeisland.cubeengine.core.user.event;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.persistence.event.ModelEvent;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.HandlerList;

/**
 *
 * @author CubeIsland-Dev
 */
public class UserEvent extends ModelEvent
{
    private static final HandlerList handlers = new HandlerList();
    private final User user;
    
    public UserEvent(CubeCore core, User user) 
    {
        super(core, user);
        this.user = user;
    }

}
