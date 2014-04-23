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
package de.cubeisland.engine.log.action;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.player.item.container.ContainerType;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link InventoryMoveItemEvent}
 * <p>Actions:
 * {@link ItemMove}
 */
public class ListenerItemMove extends LogListener
{
    public ListenerItemMove(Log module)
    {
        super(module, ItemMove.class);
    }

    @EventHandler
    public void onItemMove(InventoryMoveItemEvent event)
    {
        Inventory source = event.getSource();
        Inventory target = event.getDestination();

        if (target == null || source == null)
        {
            this.module.getLog().warn("InventoryMoveItem has null {} -> {} This should never happen!", source, target);
            return;
        }
        Location sourceLocation = this.getLocationForHolder(source.getHolder());
        if (sourceLocation == null)
        {
            return;
        }
        Location targetLocation = this.getLocationForHolder(target.getHolder());
        if (targetLocation == null)
        {
            return;
        }
        ItemMove action = this.newAction(ItemMove.class, targetLocation.getWorld());
        if (action != null)
        {
            LoggingConfiguration config = this.getConfig(targetLocation.getWorld());
            if (config.container.moveIgnore.contains(event.getItem().getType()))
            {
                return;
            }
            action.item = event.getItem();
            action.setLocation(sourceLocation);
            action.direction = sourceLocation.getBlock().getFace(targetLocation.getBlock());
            action.fromContainer = new ContainerType(source.getHolder());
            action.toContainer = new ContainerType(target.getHolder());
            this.logAction(action);
        }
    }

    private Location getLocationForHolder(InventoryHolder holder)
    {
        if (holder instanceof Entity)
        {
            return ((Entity)holder).getLocation();
        }
        else if (holder instanceof DoubleChest)
        {
            //((Chest)inventory.getLeftSide().getHolder()).getLocation()
            return ((DoubleChest)holder).getLocation();
            //TODO get the correct chest
        }
        else if (holder instanceof BlockState)
        {
            return ((BlockState)holder).getLocation();
        }
        if (holder != null)
        {
            this.module.getLog().warn("Unknown InventoryHolder: {}", holder.getClass().getName());
        }
        return null;
    }
}
