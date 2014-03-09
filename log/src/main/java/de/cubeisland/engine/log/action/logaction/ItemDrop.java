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
package de.cubeisland.engine.log.action.logaction;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.container.ContainerType;
import de.cubeisland.engine.log.storage.ItemData;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.ITEM;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;
import static org.bukkit.Material.AIR;

/**
 * dropping items
 * <p>Events: {@link PlayerDropItemEvent},
 * {@link de.cubeisland.engine.log.action.logaction.block.player.BlockBreak BlockBreak} when breaking inventory-holders</p>
 */
public class ItemDrop extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ITEM));
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public String getName()
    {
        return "item-drop";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            String itemData = new ItemData(event.getItemDrop().getItemStack()).serialize(this.om);
            this.logSimple(event.getPlayer(),itemData);
        }
    }

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


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
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
        if (logEntry.hasCauserUser())
        {
            if (logEntry.getBlock() != null)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} let drop {amount} {name#item} from {name#container}{}", time, logEntry.getCauserUser().getDisplayName(), amount, logEntry.getItemData(), logEntry.getContainerTypeFromBlock(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} dropped {amount} {name#item}{}", time, logEntry.getCauserUser().getDisplayName(), amount, logEntry.getItemData(), loc);
            }
        }
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
    }


    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getWorld() == other.getWorld()
            && logEntry.getCauser().equals(other.getCauser())
            && logEntry.getItemData().equals(other.getItemData());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_DROP_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false; // TODO possible but why?
    }
}
