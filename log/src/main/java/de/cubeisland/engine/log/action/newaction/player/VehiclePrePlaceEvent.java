package de.cubeisland.engine.log.action.newaction.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class VehiclePrePlaceEvent
{
    private Location location;
    private Entity player;

    public Location getLocation()
    {
        return location;
    }

    public Entity getPlayer()
    {
        return player;
    }
}
