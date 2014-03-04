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

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.ItemData;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.ITEM;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * picking up items
 * <p>Events: {@link PlayerPickupItemEvent}</p>
 */
public class ItemPickup extends SimpleLogActionType
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
        return "item-pickup";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            String itemData = new ItemData(event.getItem().getItemStack()).serialize(this.om);
            this.logSimple(event.getPlayer(),itemData);
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
        user.sendTranslated("%s&2%s&a picked up %d &6%s%s",
                            time,logEntry.getCauserUser().getDisplayName(),
                            amount, logEntry.getItemData(), loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getItemData().equals(other.getItemData());
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_PICKUP_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
