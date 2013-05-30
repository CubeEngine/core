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
package de.cubeisland.cubeengine.log.action.logaction.container;

import java.util.EnumSet;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.LogAttachment;
import de.cubeisland.cubeengine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.*;

/**
 * Removing items from a container
 * <p>Events: {@link ContainerActionType}
 */
public class ItemRemove extends SimpleLogActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(PLAYER, INVENTORY, ITEM);
    }

    @Override
    public String getName()
    {
        return "item-remove";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        ItemData itemData= logEntry.getItemData();
        // TODO attached
        user.sendTranslated("%s&2%s&a took &6%d %s&a out of &6%s%s&a!",
                            time, logEntry.getCauserUser().getDisplayName(),
                            -itemData.amount,itemData,
                            logEntry.getContainerTypeFromBlock(),loc);
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_REMOVE_enable;
    }

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
                HashMap<Integer,ItemStack> couldNotRemove = holder.getInventory().addItem(itemData.toItemStack());
                if (!couldNotRemove.isEmpty())
                {
                    if (force)
                    {
                        attachment.getHolder().sendTranslated("&cCould not rollback an item-remove!");
                    }
                    return false;
                }
                return true;
            }
            if (force)
            {
                attachment.getHolder().sendTranslated("&cInvalid Container to rollback item-remove!");
            }
            return false;
        }
        return false;
    }
    // TODO furnace Minecart different event?

    @Override
    public boolean canRollback()
    {
        return true;
    }
}
