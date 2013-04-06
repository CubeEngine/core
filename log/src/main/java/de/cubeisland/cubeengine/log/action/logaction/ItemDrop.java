package de.cubeisland.cubeengine.log.action.logaction;

import java.util.EnumSet;

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

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.ITEM;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;
import static org.bukkit.Material.AIR;

/**
 * dropping items
 * <p>Events: {@link PlayerDropItemEvent},
 * {@link de.cubeisland.cubeengine.log.action.logaction.block.player.BlockBreak BlockBreak} when breaking inventory-holders</p>
 */
public class ItemDrop extends SimpleLogActionType

{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER,ITEM);
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
            this.logSimple(location,player,itemData);
        }
    }


    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int amount;
        if (logEntry.hasAttached())
        {
            amount = 42; //TODO iterate and get correct amount
        }
        else
        {
            amount = logEntry.getItemData().amount;
        }
        if (logEntry.hasCauserUser())
        {
            user.sendTranslated("%s&2%s&a dropped %d &6%s%s!",
                                time, logEntry.getCauserUser().getDisplayName(),
                                amount, logEntry.getItemData(),loc);
        }
        else
        {
            user.sendTranslated("%s&2%s&a dropped %d &6%s%s!",
                                time, logEntry.getCauserEntity(),
                                amount, logEntry.getItemData(),loc);
        }

    }


    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.getItemData().equals(other.getItemData());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_DROP_enable;
    }
}
