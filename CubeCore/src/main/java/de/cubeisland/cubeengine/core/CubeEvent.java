package de.cubeisland.cubeengine.core;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 *
 * @author CubeIsland-Dev
 */
public abstract class CubeEvent extends Event implements Cancellable
{
    private final CubeCore core;
    private boolean cancelled;
    
    public CubeEvent(CubeCore core) 
    {
        this.core = core;
    }
    
    public CubeCore getCore()
    {
        return this.core;
    }
    
    public boolean isCancelled()
    {
        return this.cancelled;
    }
    
    public void setCancelled(boolean bln)
    {
        this.cancelled = bln;
    }
}
