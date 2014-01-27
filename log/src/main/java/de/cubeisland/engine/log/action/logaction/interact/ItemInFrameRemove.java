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
package de.cubeisland.engine.log.action.logaction.interact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.ItemData;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

public class ItemInFrameRemove extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY, ITEM));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemRemove(EntityDamageByEntityEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            if (event.getEntity() instanceof ItemFrame)
            {
                if (((ItemFrame)event.getEntity()).getItem() != null)
                {
                    this.logSimple(event.getEntity().getLocation(), event.getDamager(), new ItemData(((ItemFrame)event.getEntity()).getItem()).serialize(this.om));
                }
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        ItemData itemData= logEntry.getItemData();
        if (logEntry.getCauser() <= 0)
        {
            if (logEntry.hasAttached())
            {
                user.sendTranslated("%s&aSomething removed &6%d %s&a from itemframes%s",
                                    time, logEntry.getAttached().size() +1, itemData, loc);
            }
            else
            {
                user.sendTranslated("%s&aaSomething removed &6%s&a from an itemframe%s",
                                    time, itemData, loc);
            }
        }
        else
        {
            if (logEntry.hasAttached())
            {
                user.sendTranslated("%s&2%s&a removed &6%d %s&a from itemframes%s",
                                    time, logEntry.getCauserUser().getName(), logEntry.getAttached().size() +1, itemData, loc);
            }
            else
            {
                user.sendTranslated("%s&2%s&a removed &6%s&a from an itemframe%s",
                                    time, logEntry.getCauserUser().getName(), itemData, loc);
            }
        }
    }

    @Override
    public String getName()
    {
        return "remove-item";
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ITEM_REMOVE_FROM_FRAME;
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return super.isSimilar(logEntry, other) && logEntry.getAdditionaldata().equals(other.getAdditionaldata());
    }

    // TODO redo & rollback possible if entity still exists || or entity at position

    @Override
    public boolean canRollback()
    {
        return false;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }
}
