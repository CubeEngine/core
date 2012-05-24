package de.cubeisland.cubeengine.core.event;

import de.cubeisland.cubeengine.core.CubeCore;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author CubeIsland-Dev
 */
public class CubeEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();
    private final CubeCore core;
    
    public CubeEvent(CubeCore core) 
    {
        this.core = core;
    }
    
    public CubeCore getCore()
    {
        return this.core;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
