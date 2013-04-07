package de.cubeisland.cubeengine.log.action.logaction.interact;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENTITY;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;


/**
 * Exiting vehicles
 * <p>Events: {@link VehicleExitEvent}
 */
public class VehicleExit extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, ENTITY);
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "vehicle-exit";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event)
    {
        if (this.isActive(event.getVehicle().getWorld()))
        {
            this.logSimple(event.getVehicle().getLocation(),event.getExited(),event.getVehicle(),null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.getCauserUser() == null)
        {
            user.sendTranslated("%s&6%s &aexited a &6%s%s&a!",
                                time, logEntry.getCauserEntity(),
                                logEntry.getEntityFromData(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aexited a &6%s%s&a!",
                                time, logEntry.getCauserUser().getDisplayName(),
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

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).VEHICLE_EXIT_enable;
    }
}
