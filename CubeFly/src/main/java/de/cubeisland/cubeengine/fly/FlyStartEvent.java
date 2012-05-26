package de.cubeisland.cubeengine.fly;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.event.UserEvent;

/**
 *
 * @author Faithcaio
 */
public class FlyStartEvent extends UserEvent
{
    public FlyStartEvent(CubeCore core, User user)
    {
        super(core,user);
    }
}
