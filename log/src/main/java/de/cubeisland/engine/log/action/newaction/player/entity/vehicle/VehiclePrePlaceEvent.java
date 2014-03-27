package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class VehiclePrePlaceEvent // TODO
{
    public VehiclePrePlaceEvent(Location location, Entity player)
    {
        this.location = location;
        this.player = player;
    }

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
