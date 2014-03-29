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
package de.cubeisland.engine.log.action.newaction.player.entity.vehicle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.projectiles.ProjectileSource;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.LogListener;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link VehicleDestroyEvent}
 * {@link VehicleEnterEvent}
 * {@link VehicleExitEvent}
 * {@link VehicleCreateEvent}
 * <p>PrePlan Events:
 * {@link VehiclePrePlaceEvent}
 * <p>Actions:
 * {@link PlayerVehicleBreak}
 * {@link PlayerVehicleEnter}
 * {@link PlayerVehicleExit}
 * {@link PlayerVehiclePlace}
 */
public class PlayerVehicleListener extends LogListener
{
    private transient final Map<Location, Entity> plannedVehiclePlace = new ConcurrentHashMap<>();
    private transient volatile boolean clearPlanned = false;

    public PlayerVehicleListener(Log module)
    {
        super(module);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event)
    {
        Entity causer;
        if (event.getAttacker() != null)
        {
            if (event.getAttacker() instanceof Player)
            {
                causer = event.getAttacker();
            }
            else if (event.getAttacker() instanceof Projectile)
            {
                ProjectileSource shooter = ((Projectile)event.getAttacker()).getShooter();
                if (shooter instanceof Player)
                {
                    causer = (Player)shooter;
                }
                else if (shooter instanceof Entity)
                {
                    causer = (Entity)shooter;
                }
                else
                {
                    return; // TODO other ProjectileSources
                }
            }
            else
            {
                causer = event.getAttacker();
            }
        }
        else if (event.getVehicle().getPassenger() instanceof Player)
        {
            causer = event.getVehicle().getPassenger();
        }
        else
        {
            return; // TODO why?
        }
        if (causer instanceof Player)
        {
            PlayerVehicleBreak action = this.newAction(PlayerVehicleBreak.class, event.getVehicle().getWorld());
            if (action != null)
            {
                action.setLocation(event.getVehicle().getLocation());
                action.setVehicle(event.getVehicle());
                action.setPlayer((Player)causer);
                this.logAction(action);
            }
        }
        else
        {
            // TODO EntityVehicleBreak
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(final VehicleEnterEvent event)
    {
        if (event.getEntered() instanceof Player)
        {
            PlayerVehicleEnter action = this.newAction(PlayerVehicleEnter.class, event.getEntered().getWorld());
            if (action != null)
            {
                action.setVehicle(event.getVehicle());
                action.setLocation(event.getVehicle().getLocation());
                action.setPlayer((Player)event.getEntered());
                this.logAction(action);
            }
        }
        else
        {
            // TODO entity vehicle enter
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(final VehicleExitEvent event)
    {
        if (event.getExited() instanceof Player)
        {
            PlayerVehicleExit action = this.newAction(PlayerVehicleExit.class, event.getExited().getWorld());
            if (action != null)
            {
                action.setVehicle(event.getVehicle());
                action.setLocation(event.getVehicle().getLocation());
                action.setPlayer((Player)event.getExited());
                this.logAction(action);
            }
        }
        else
        {
            // TODO entity vehicle exit
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleCreate(final VehicleCreateEvent event)
    {
        Location location = event.getVehicle().getLocation();
        Entity entity = this.plannedVehiclePlace.get(location);
        if (entity != null)
        {
            if (entity instanceof Player)
            {
                PlayerVehiclePlace action = this.newAction(PlayerVehiclePlace.class, event.getVehicle().getWorld());
                if (action != null)
                {
                    action.setVehicle(event.getVehicle());
                    action.setLocation(event.getVehicle().getLocation());
                    action.setPlayer((Player)entity);
                    this.logAction(action);
                }
            }
            else
            {
                // TODO entity place?
            }
        }
        // else not a create by placement -> ignore
    }

    @EventHandler
    public void onVehiclePreplan(final VehiclePrePlaceEvent event)
    {
        // TODO so call me maybe?
        plannedVehiclePlace.put(event.getLocation(), event.getPlayer());
        if (!clearPlanned)
        {
            clearPlanned = true;
            module.getCore().getTaskManager().runTask(module, new Runnable()
            {
                @Override
                public void run()
                {
                    clearPlanned = false;
                    plannedVehiclePlace.clear();
                }
            });
        }
    }
}
