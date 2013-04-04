package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;

public class VehiclePlace extends LogActionType
{
    public VehiclePlace(Log module)
    {
        super(module, 0x60, "vehicle-place");
    }

    public void preplanVehiclePlacement(Location location, Player player)
    {
        //TODO
    }
}
