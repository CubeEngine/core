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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * Breaking vehicles
 * <p>Events: {@link VehicleDestroyEvent}
 */
public class VehicleBreak extends SimpleLogActionType
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
        return "vehicle-break";
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

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&2%s&a broke a &6%s%s",
                            time, logEntry.getCauserUser() == null ?
                            logEntry.getCauserEntity() :
                            logEntry.getCauserUser().getDisplayName(),
                            logEntry.getEntityFromData(),loc);
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
        return this.lm.getConfig(world).VEHICLE_BREAK_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false; // TODO might be possible
    }
}
