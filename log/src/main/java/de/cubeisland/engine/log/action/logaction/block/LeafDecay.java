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
package de.cubeisland.engine.log.action.logaction.block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.LeavesDecayEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.ENVIRONEMENT;
import static org.bukkit.Material.AIR;

/**
 * Leaves decaying
 * <p>Events: {@link LeavesDecayEvent}
 */
public class LeafDecay extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, ENVIRONEMENT));
    }

    @Override
    public String getName()
    {
        return "leaf-decay";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            this.logBlockChange(event.getBlock().getLocation(),null,
                                BlockData.of(event.getBlock().getState()),
                                AIR,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated(MessageType.POSITIVE, "{}{name#block} decayed{}", time, logEntry.getOldBlock(), loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.LEAF_DECAY_enable;
    }

}
