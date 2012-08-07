package de.cubeisland.cubeengine.core;

import de.cubeisland.cubeengine.BukkitDependend;
import org.bukkit.event.Event;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("This extends the BukkitEvent")
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