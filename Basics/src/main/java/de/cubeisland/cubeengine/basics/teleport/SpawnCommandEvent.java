package de.cubeisland.cubeengine.basics.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserEvent;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class SpawnCommandEvent extends UserEvent
{
    public SpawnCommandEvent(Basics basics, User user, Location loc)
    {
        super(basics.getCore(), user);
        this.basics = basics;
        this.loc = loc;
    }

    private Location loc;
    private final Basics basics;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * @return the loc
     */
    public Location getLoc()
    {
        return loc;
    }

    /**
     * @param loc the loc to set
     */
    public void setLoc(Location loc)
    {
        this.loc = loc;
    }

    /**
     * @return the basics
     */
    public Basics getBasics()
    {
        return basics;
    }
}
