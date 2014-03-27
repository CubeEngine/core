package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import org.bukkit.Location;

import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

public class HangingPreBreakEvent // TODO
{
    public HangingPreBreakEvent(Location location, ActionTypeBase cause)
    {
        this.location = location;
        this.cause = cause;
    }

    private Location location;
    private ActionTypeBase cause;

    public Location getLocation()
    {
        return location;
    }

    public ActionTypeBase getCause()
    {
        return cause;
    }
}
