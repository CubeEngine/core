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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * Placing vehicles
 * <p>Events: {@link VehicleCreateEvent}
 * {@link de.cubeisland.engine.log.action.logaction.block.interaction.RightClickActionType preplanned place}
 */
public class VehiclePlace extends SimpleLogActionType
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
        return "vehicle-place";
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
            this.logModule.getLog().info("Unexpected VehiclePlacement: {} planned: {}", event.getVehicle(),
                                         plannedVehiclePlace.size());
        }
    }

    private volatile boolean clearPlanned = false;
    private Map<Location,Entity> plannedVehiclePlace = new ConcurrentHashMap<>();

    public void preplanVehiclePlacement(Location location, Player player)
    {
        plannedVehiclePlace.put(location, player);
        if (!clearPlanned)
        {
            clearPlanned = true;
            VehiclePlace.this.logModule.getCore().getTaskManager().runTask(logModule, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    VehiclePlace.this.plannedVehiclePlace.clear();
                }
            });
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        // TODO NPE here
        user.sendTranslated("%s&2%s &aplaced a &6%s%s",
                            time,logEntry.getCauserUser().getDisplayName(),
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
        return this.lm.getConfig(world).VEHICLE_PLACE_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
