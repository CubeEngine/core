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
package de.cubeisland.engine.log.action.player.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.material.Dye;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.player.ListenerPlayerLog;

import static org.bukkit.Material.*;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link PlayerInteractEntityEvent}
 * {@link PlayerShearEntityEvent}
 * <p>Actions:
 * {@link UseFurnaceMinecart}
 * {@link EntityDye}
 * {@link EntityFillSoup}
 * {@link EntityShear}
 */
public class ListenerPlayerEntity extends ListenerPlayerLog
{
    public ListenerPlayerEntity(Log module)
    {
        super(module, UseFurnaceMinecart.class, EntityDye.class, EntityFillSoup.class, EntityShear.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof LivingEntity))
        {
            return;
        }
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        ActionPlayerEntity action;
        if (player.getItemInHand().getType() == COAL && entity instanceof PoweredMinecart)
        {
            action = this.newAction(UseFurnaceMinecart.class, entity.getWorld());
        }
        else if (player.getItemInHand().getType() == INK_SACK && entity instanceof Sheep || entity instanceof Wolf)
        {
            action = this.newAction(EntityDye.class, entity.getWorld());
            if (action != null)
            {
                ((EntityDye)action).setColor(((Dye)player.getItemInHand().getData()).getColor());
            }
        }
        else if (player.getItemInHand().getType().equals(BOWL) && entity instanceof MushroomCow)
        {
            action = this.newAction(EntityFillSoup.class, entity.getWorld());
        }
        else
        {
            return;
        }
        if (action != null)
        {
            action.setEntity(entity);
            action.setPlayer(player);
            action.setLocation(entity.getLocation());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShear(PlayerShearEntityEvent event)
    {
        EntityShear action = this.newAction(EntityShear.class, event.getEntity().getWorld());
        if (action != null)
        {
            action.setEntity(event.getEntity());
            action.setPlayer(event.getPlayer());
            action.setLocation(event.getEntity().getLocation());
            this.logAction(action);
        }
    }
}
