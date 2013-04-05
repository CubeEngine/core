package de.cubeisland.cubeengine.log.action.logaction.interact;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;

import static de.cubeisland.cubeengine.log.storage.ActionType.VEHICLE_PLACE;

public class VehiclePlace extends SimpleLogActionType
{
    public VehiclePlace(Log module)
    {
        super(module, 0x60, "vehicle-place");
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            Location location = event.getVehicle().getLocation();
            Entity player = this.plannedVehiclePlace.get(location);
            this.logSimple(location,player,event.getVehicle(),null);
        }
        else
        {
            System.out.print("Unexpected VehiclePlacement: "+event.getVehicle()+
                                 " planned: "+plannedVehiclePlace.size());
        }
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Entity> plannedVehiclePlace = new ConcurrentHashMap<Location,Entity>();

    public void preplanVehiclePlacement(Location location, Player player)
    {
        plannedVehiclePlace.put(location, player);
        if (!clearPlanned)
        {
            clearPlanned = true;
            VehiclePlace.this.logModule.getCore().getTaskManager().scheduleSyncDelayedTask(logModule, new Runnable() {
                @Override
                public void run() {
                    clearPlanned = false;
                    VehiclePlace.this.plannedVehiclePlace.clear();
                }
            });
        }
    }
}
