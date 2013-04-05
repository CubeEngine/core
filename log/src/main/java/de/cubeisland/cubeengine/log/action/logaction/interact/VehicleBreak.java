package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

import static de.cubeisland.cubeengine.log.storage.ActionType.VEHICLE_BREAK;

public class VehicleBreak extends SimpleLogActionType
{
    public VehicleBreak(Log module)
    {
        super(module, 0x62, "vehicle-break");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            Entity causer = null;
            if (event.getAttacker() != null)
            {
                if (event.getAttacker() instanceof Player)
                {
                    causer = event.getAttacker();
                }
                else if (event.getAttacker() instanceof Projectile)
                {
                    Projectile projectile = (Projectile) event.getAttacker();
                    if (projectile.getShooter() instanceof Player)
                    {
                        causer = projectile.getShooter();
                    }
                    else if (projectile.getShooter() != null)
                    {
                        causer = projectile.getShooter();
                    }
                }
            }
            else if (event.getVehicle().getPassenger() instanceof Player)
            {
                causer = event.getVehicle().getPassenger();
            }
            this.logSimple(event.getVehicle().getLocation(),causer,event.getVehicle(),null);
        }
    }
}
