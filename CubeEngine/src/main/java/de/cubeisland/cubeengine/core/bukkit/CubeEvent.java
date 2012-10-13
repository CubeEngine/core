package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * This class is a custom Event containing the core to allow easy access.
 */
public abstract class CubeEvent extends Event implements Cancellable
{
    private final Core core;
    private boolean cancelled;

    public CubeEvent(Core core)
    {
        this.core = core;
    }

    /**
     * Returns the CubeEngine-Core
     *
     * @return the core
     */
    public Core getCore()
    {
        return this.core;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bln)
    {
        this.cancelled = bln;
    }
}