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

import org.bukkit.DyeColor;
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

import de.cubeisland.engine.log.action.logaction.ActionTypeContainer;
import de.cubeisland.engine.log.action.logaction.container.ItemInsert;
import de.cubeisland.engine.log.storage.ItemData;

import static org.bukkit.Material.*;

/**
 * Container-ActionType for interaction
 * <p>Events: {@link PlayerInteractEntityEvent}</p>
 * <p>External Actions:
 * {@link ItemInsert},
 * {@link EntityDye},
 * {@link SoupFill}
 */
public class InteractEntityActionType extends ActionTypeContainer
{
    public InteractEntityActionType()
    {
        super("INTERACT_ENTITY");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event)
    {
        if (!(event.getRightClicked() instanceof LivingEntity)) return;
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        if (player.getItemInHand().getType().equals(COAL) && entity instanceof PoweredMinecart)
        {
            ItemInsert itemInsert = this.manager.getActionType(ItemInsert.class);
            if (itemInsert.isActive(player.getWorld()))
            {
                ItemData itemData = new ItemData(player.getItemInHand());
                itemData.amount = 1;
                itemInsert.logSimple(entity.getLocation(),player,entity,itemData.serialize(this.om));
            }
        }
        else if(player.getItemInHand().getType().equals(INK_SACK) && entity instanceof Sheep || entity instanceof Wolf)
        {
            EntityDye entityDye = this.manager.getActionType(EntityDye.class);
            if (entityDye.isActive(entity.getWorld()))
            {
                String additional = entityDye.serializeData(null, entity,
                            DyeColor.getByDyeData(player.getItemInHand().getData().getData()));
                entityDye.logSimple(entity.getLocation(),player,entity,additional);
            }
        }
        else if (player.getItemInHand().getType().equals(BOWL) && entity instanceof MushroomCow)
        {
            SoupFill soupFill = this.manager.getActionType(SoupFill.class);
            if (soupFill.isActive(player.getWorld()))
            {
                soupFill.logSimple(entity.getLocation(),player,entity,null);
            }
        }
    }
}
