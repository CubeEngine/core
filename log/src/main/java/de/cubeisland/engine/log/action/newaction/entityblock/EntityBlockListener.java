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
package de.cubeisland.engine.log.action.newaction.entityblock;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import de.cubeisland.engine.log.action.newaction.LogListener;

import static org.bukkit.Material.AIR;

/**
 * A Listener for EntityBlock Actions
 * <p>Events:
 * {@link EntityChangeBlockEvent}
 * {@link EntityBreakDoorEvent}
 * {@link EntityBlockFormEvent}
 * <p>Actions:
 * {@link SheepEat}
 * {@link EndermanPickup}
 * {@link EndermanPlace}
 * {@link OtherEntityChangeBlock}
 * {@link EntityBreakBlock} // TODO other events?
 * {@link EntityForm}
 */
public class EntityBlockListener extends LogListener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event)
    {
        EntityBlockActionType action;
        if(event.getEntityType().equals(EntityType.SHEEP))
        {
            action = this.newAction(SheepEat.class, event.getBlock().getWorld());
        }
        else if(event.getEntity() instanceof Enderman)
        {
            if (event.getTo().equals(AIR))
            {
                action = this.newAction(EndermanPickup.class, event.getBlock().getWorld());
            }
            else
            {
                action = this.newAction(EndermanPlace.class, event.getBlock().getWorld());
            }
        }
        else
        {
            action = this.newAction(OtherEntityChangeBlock.class, event.getBlock().getWorld());
        }
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setEntity(event.getEntity());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getTo());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        EntityBlockActionType action = this.newAction(EntityBreakBlock.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setEntity(event.getEntity());
            BlockState state = event.getBlock().getState();
            state = this.adjustBlockForDoubleBlocks(state);
            // TODO adjust for Door
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getTo());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event)
    {
        EntityBlockActionType action = this.newAction(EntityForm.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setLocation(event.getBlock().getLocation());
            action.setEntity(event.getEntity());
            action.setOldBlock(event.getBlock().getState());
            action.setNewBlock(event.getNewState());
            this.logAction(action);
        }
    }
}
