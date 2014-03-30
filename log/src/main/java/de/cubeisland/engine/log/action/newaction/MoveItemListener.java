package de.cubeisland.engine.log.action.newaction;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.log.action.newaction.block.player.interact.container.ContainerType;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link InventoryMoveItemEvent}
 * <p>Actions:
 * {@link MoveItem}
 */
public class MoveItemListener extends LogListener
{
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
        MoveItem action = this.newAction(MoveItem.class, targetLocation.getWorld());
        if (action != null)
        {
            /* TODO
            if (this.lm.getConfig(targetLocation.getWorld()).container.ITEM_TRANSFER_ignore
                .contains(event.getItem().getType()))
            */
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
