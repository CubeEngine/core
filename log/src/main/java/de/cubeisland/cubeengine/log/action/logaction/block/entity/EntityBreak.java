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
package de.cubeisland.cubeengine.log.action.logaction.block.entity;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityBreakDoorEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK_ENTITY;
import static org.bukkit.Material.AIR;

/**
 * Usually Zombies breaking doors.
 * <p>Events: {@link EntityBreakDoorEvent}</p>
 */
public class EntityBreak extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, BLOCK_ENTITY);
    }

    @Override
    public String getName()
    {
        return "entity-break";
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityBreakDoor(final EntityBreakDoorEvent event)
    {
        if (this.isActive(event.getEntity().getWorld()))
        {
            BlockState state = event.getBlock().getState();
            state = this.adjustBlockForDoubleBlocks(state);
            this.logBlockChange(state.getLocation(),event.getEntity(),BlockData.of(state),AIR,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&aA &6%s &adestroyed &6%s%s",
                            time,
                            logEntry.getCauserEntity(),
                            logEntry.getOldBlock(),
                            loc);
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).ENTITY_BREAK_enable;
    }
}
