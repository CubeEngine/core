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
package de.cubeisland.engine.log.action.player.item.container;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.player.item.ActionItem;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

public abstract class ActionContainerItem extends ActionItem
{
    public ContainerType type;

    protected ActionContainerItem(String name)
    {
        super(name, ITEM);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ActionContainerItem && this.player.equals(((ActionContainerItem)action).player)
            && this.coord.equals(action.coord) && this.type.equals(((ActionContainerItem)action).type)
            && this.item.isSimilar(((ActionContainerItem)action).item);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = this.item.getAmount();
        if (this instanceof ItemRemove)
        {
            amount *= -1;
        }
        if (this.hasAttached())
        {
            for (BaseAction action : this.getAttached())
            {
                if (action instanceof ItemInsert)
                {
                    amount += ((ItemInsert)action).item.getAmount();
                }
                else if (action instanceof ItemRemove)
                {
                    amount -= ((ItemRemove)action).item.getAmount();
                }
            }
        }
        if (amount > 0)
        {
            return user.getTranslation(POSITIVE, "{user} placed {amount} {name#item} into {name#container}",
                                       this.player.name, amount, this.item.getType().name(), this.type.name);
        }
        if (amount < 0)
        {
            return user.getTranslation(POSITIVE, "{user} took {amount} {name#item} out of {name#container}",
                                       this.player.name, -amount, this.item.getType().name(), this.type.name);
        }
        return user.getTranslation(POSITIVE, "{user} did not change the amount of {name#item} in {name#container}",
                                   this.player.name, this.item.getType().name(), this.type.name);
    }

    /*

    @Override
    public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        Location loc = logEntry.getLocation();
        Material material = logEntry.getContainerTypeFromBlock().getMaterial();
        if (material.equals(Material.STORAGE_MINECART))
        {
            // TODO MinecartInventoryHolders  // would need UUID
        }
        else
        {
            BlockState block = loc.getBlock().getState();
            if (block instanceof InventoryHolder && block.getType().equals(material)) // Same container
            {
                ItemData itemData = logEntry.getItemData();
                InventoryHolder holder = (InventoryHolder)block;
                HashMap<Integer, ItemStack> couldNotRemove = holder.getInventory().removeItem(itemData.toItemStack());
                if (!couldNotRemove.isEmpty())
                {
                    attachment.getHolder().sendTranslated(MessageType.NEGATIVE, "Could not rollback an item-insert!");
                    return false;
                }
                return true;
            }
            if (force)
            {
                attachment.getHolder().sendTranslated(MessageType.NEGATIVE,
                                                      "Invalid Container to rollback item-insert!");
            }
            return false;
        }
        return false;
    }
    // TODO remove duplicated logic from itemInsert & ItemRemove
    @Override
    public boolean redo(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        Location loc = logEntry.getLocation();
        Material material = logEntry.getContainerTypeFromBlock().getMaterial();
        if (material.equals(Material.STORAGE_MINECART))
        {
            // TODO MinecartInventoryHolders  // would need UUID
        }
        else
        {
            BlockState block = loc.getBlock().getState();
            if (block instanceof InventoryHolder && block.getType().equals(material)) // Same container
            {
                ItemData itemData = logEntry.getItemData();
                InventoryHolder holder = (InventoryHolder)block;
                HashMap<Integer, ItemStack> couldNotRemove = holder.getInventory().addItem(itemData.toItemStack());
                if (!couldNotRemove.isEmpty())
                {
                    attachment.getHolder().sendTranslated(MessageType.NEGATIVE, "Could not rollback an item-insert!");
                    return false;
                }
                return true;
            }
            if (force)
            {
                attachment.getHolder().sendTranslated(MessageType.NEGATIVE,
                                                      "Invalid Container to rollback item-insert!");
            }
            return false;
        }
        return false;
    }





     */

    /*
    @Override
     public boolean rollback(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
{
    Location loc = logEntry.getLocation();
    Material material = logEntry.getContainerTypeFromBlock().getMaterial();
    if (material.equals(Material.STORAGE_MINECART))
    {
        // TODO MinecartInventoryHolders
    }
    else
    {
        BlockState block = loc.getBlock().getState();
        if (block instanceof InventoryHolder && block.getType().equals(material)) // Same containertype
        {
            ItemData itemData = logEntry.getItemData();
            InventoryHolder holder = (InventoryHolder)block;
            itemData.amount = -itemData.amount;
            HashMap<Integer, ItemStack> couldNotRemove = holder.getInventory().addItem(itemData.toItemStack());
            if (!couldNotRemove.isEmpty())
            {
                if (force)
                {
                    attachment.getHolder()
                              .sendTranslated(MessageType.NEGATIVE, "Could not rollback an item-remove!");
                }
                return false;
            }
            return true;
        }
        if (force)
        {
            attachment.getHolder()
                      .sendTranslated(MessageType.NEGATIVE, "Invalid Container to rollback item-remove!");
        }
        return false;
    }
    return false;
}

    @Override
    public boolean redo(LogAttachment attachment, LogEntry logEntry, boolean force, boolean preview)
    {
        Location loc = logEntry.getLocation();
        Material material = logEntry.getContainerTypeFromBlock().getMaterial();
        if (material.equals(Material.STORAGE_MINECART))
        {
            // TODO MinecartInventoryHolders
        }
        else
        {
            BlockState block = loc.getBlock().getState();
            if (block instanceof InventoryHolder && block.getType().equals(material)) // Same containertype
            {
                ItemData itemData = logEntry.getItemData();
                InventoryHolder holder = (InventoryHolder)block;
                itemData.amount = -itemData.amount;
                HashMap<Integer, ItemStack> couldNotRemove = holder.getInventory().removeItem(itemData.toItemStack());
                if (!couldNotRemove.isEmpty())
                {
                    if (force)
                    {
                        attachment.getHolder()
                                  .sendTranslated(MessageType.NEGATIVE, "Could not rollback an item-remove!");
                    }
                    return false;
                }
                return true;
            }
            if (force)
            {
                attachment.getHolder()
                          .sendTranslated(MessageType.NEGATIVE, "Invalid Container to rollback item-remove!");
            }
            return false;
        }
        return false;
    }
     */
}
