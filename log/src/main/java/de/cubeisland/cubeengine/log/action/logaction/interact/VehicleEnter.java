package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

public class VehicleEnter extends SimpleLogActionType
{
    public VehicleEnter(Log module)
    {
        super(module, 0x53, "vehicle-enter");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            this.logSimple(event.getVehicle().getLocation(),event.getEntered(),event.getVehicle(),null);
        }
    }
}
