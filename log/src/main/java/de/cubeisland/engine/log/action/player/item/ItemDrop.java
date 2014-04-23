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
package de.cubeisland.engine.log.action.player.item;

import org.bukkit.entity.Item;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.entity.ActionEntityBlock.EntitySection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents a player dropping an item
 */
public class ItemDrop extends ActionItem
{
    public EntitySection entity;

    public ItemDrop()
    {
        super("drop", ITEM);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ItemDrop && this.player.equals(((ItemDrop)action).player)
            && ((ItemDrop)action).item.isSimilar(this.item);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = this.item.getAmount();
        if (this.hasAttached())
        {
            for (BaseAction action : this.getAttached())
            {
                amount += ((ItemDrop)action).item.getAmount();
            }
        }
        return user.getTranslation(POSITIVE, "{user} dropped {name#item} x{amount}", this.player.name,
                                   this.item.getType().name(), amount);
    }

    public void setItem(Item item)
    {
        this.setItemstack(item.getItemStack());
        this.entity = new EntitySection(item);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.item.drop_manual;
    }

    // TODO chestDrop Action
    /*
    public void logDropsFromChest(InventoryHolder containerBlock, Location location, Player player)
    {
        ItemStack[] contents;
        if (containerBlock.getInventory() instanceof DoubleChestInventory)
        {
            DoubleChestInventory inventory = (DoubleChestInventory) containerBlock.getInventory();
            if (((Chest)inventory.getLeftSide().getHolder()).getLocation().equals(location))
            {
                contents = inventory.getLeftSide().getContents();
            }
            else
            {
                contents = inventory.getRightSide().getContents();
            }
        }
        else
        {
            contents = containerBlock.getInventory().getContents();
        }
        for (ItemStack itemStack : contents)
        {
            if (itemStack == null || itemStack.getType().equals(AIR))
            {
                continue;
            }
            String itemData = new ItemData(itemStack).serialize(this.om);
            this.logSimple(location,player,new ContainerType(containerBlock), itemData);
        }
    }
     */
    // TODO EntityDrop Action

    /*
        if (logEntry.getBlock() != null)
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{user} let drop {amount} {name#item} from {name#container}{}", time, logEntry.getCauserUser().getDisplayName(), amount, logEntry.getItemData(), logEntry.getContainerTypeFromBlock(), loc);
        }
     */
    /*
        int amount;
        if (logEntry.hasAttached())
        {
            amount = logEntry.getItemData().amount;
            for (LogEntry entry : logEntry.getAttached())
            {
                amount += entry.getItemData().amount;
            }
        }
        else
        {
            amount = logEntry.getItemData().amount;
        }
     */
    /*
        else
        {
            if (logEntry.getBlock() != null)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#entity} let drop {amount} {name#item} from {name#container}{}", time, logEntry.getCauserEntity(), amount, logEntry.getItemData(), logEntry.getContainerTypeFromBlock(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#entity} dropped {amount} {name#item}{}", time, logEntry.getCauserEntity(), amount, logEntry.getItemData(), loc);
            }

        }
     */
}
