/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.log.action.logaction.interact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;


/**
 * Exiting vehicles
 * <p>Events: {@link VehicleExitEvent}
 */
public class VehicleExit extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(VEHICLE, PLAYER, ENTITY));
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
            user.sendTranslated("%s&6%s &aexited a &6%s%s",
                                time, logEntry.getCauserEntity(),
                                logEntry.getEntityFromData(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s &aexited a &6%s%s",
                                time, logEntry.getCauserUser().getDisplayName(),
                                logEntry.getEntityFromData(),loc);
        }
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getWorld() == other.getWorld()
            && logEntry.getCauser().equals(other.getCauser())
            && logEntry.getData() == other.getData();
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).VEHICLE_EXIT_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
