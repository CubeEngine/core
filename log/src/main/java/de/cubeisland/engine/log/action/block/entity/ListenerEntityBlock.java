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
package de.cubeisland.engine.log.action.block.entity;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;

import static org.bukkit.Material.AIR;
import static org.bukkit.entity.EntityType.SHEEP;

/**
 * A Listener for {@link ActionEntityBlock}
 * <p>Events:
 * {@link EntityChangeBlockEvent}
 * {@link EntityBreakDoorEvent}
 * {@link EntityBlockFormEvent}
 * <p>All Actions:
 * {@link SheepEat}
 * {@link EndermanPickup}
 * {@link EndermanPlace}
 * {@link EntityChange}
 * {@link EntityBreak} // TODO other events?
 * {@link EntityForm}
 */
public class ListenerEntityBlock extends LogListener
{
    public ListenerEntityBlock(Log module)
    {
        super(module, SheepEat.class, EndermanPickup.class, EndermanPlace.class, EntityChange.class, EntityBreak.class,
              EntityForm.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(final EntityChangeBlockEvent event)
    {
        ActionEntityBlock action;
        if (event.getEntityType() == SHEEP)
        {
            action = this.newAction(SheepEat.class, event.getBlock().getWorld());
        }
        else if (event.getEntity() instanceof Enderman)
        {
            if (event.getTo() == AIR)
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
            action = this.newAction(EntityChange.class, event.getBlock().getWorld());
        }
        if (action != null)
        {
            action.setNewBlock(event.getTo());
            this.setAndLog(action, event.getBlock().getState(), event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        ActionEntityBlock action = this.newAction(EntityBreak.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setNewBlock(event.getTo());
            this.setAndLog(action, adjustBlockForDoubleBlocks(event.getBlock().getState()), event.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBlockForm(final EntityBlockFormEvent event)
    {
        ActionEntityBlock action = this.newAction(EntityForm.class, event.getBlock().getWorld());
        if (action != null)
        {
            action.setNewBlock(event.getNewState());
            this.setAndLog(action, event.getBlock().getState(), event.getEntity());
        }
    }

    private void setAndLog(ActionEntityBlock action, BlockState state, Entity entity)
    {
        action.setLocation(state.getLocation());
        action.setEntity(entity);
        action.setOldBlock(state);
        this.logAction(action);
    }
}
