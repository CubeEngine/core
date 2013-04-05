package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;


public class VehicleExit extends SimpleLogActionType
{
    public VehicleExit(Log module)
    {
        super(module, 0x54, "vehicle-exit");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            this.logSimple(event.getVehicle().getLocation(),event.getExited(),event.getVehicle(),null);
        }
    }
}
