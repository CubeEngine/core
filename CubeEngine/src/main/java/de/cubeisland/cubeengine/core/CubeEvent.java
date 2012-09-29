package de.cubeisland.cubeengine.core;

import org.bukkit.event.Event;

/**
 *
 * @author Phillip Schichtel
 */
public abstract class CubeEvent extends Event
{
    private final Core core;
    private boolean cancelled;

    public CubeEvent(Core core)
    {
        this.core = core;
    }

    public Core getCore()
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