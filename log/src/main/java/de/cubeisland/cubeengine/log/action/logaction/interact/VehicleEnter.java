package de.cubeisland.cubeengine.log.action.logaction.interact;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Type.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Type.PLAYER;

/**
 * Entering vehicles
 * <p>Events: {@link VehicleEnterEvent}
 */
public class VehicleEnter extends SimpleLogActionType
{
    public VehicleEnter(Log module)
    {
        super(module, "vehicle-enter", PLAYER, ENTITY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            this.logSimple(event.getVehicle().getLocation(),event.getEntered(),event.getVehicle(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getCauserUser() == null)
        {
            user.sendTranslated("%s&6%s &aentered a &6%s%s&a!",
                                time,logEntry.getCauserEntity(),
                                logEntry.getEntityFromData(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aentered a &6%s%s&a!",
                                time,logEntry.getCauserUser().getDisplayName(),
                                logEntry.getEntityFromData(),loc);
        }
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.data == other.data;
    }
}
