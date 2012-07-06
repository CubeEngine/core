package de.cubeisland.cubeengine.core;

import org.bukkit.event.Event;

/**
 *
 * @author CubeIsland-Dev
 */
public abstract class CubeEvent extends Event
{
    private final CubeCore core;
    
    public CubeEvent(CubeCore core) 
    {
        this.core = core;
    }
    
    public CubeCore getCore()
    {
        return this.core;
    }
}
