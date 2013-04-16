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
package de.cubeisland.cubeengine.log.action.logaction.block;

import java.util.EnumSet;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static org.bukkit.Material.AIR;

/**
 * Blocks moved by piston
 * <p>Events: {@link BlockPistonExtendEvent}, {@link BlockPistonRetractEvent}</p>
 */
public class BlockShift extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK);
    }

    @Override
    public String getName()
    {
        return "block-shift";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event)
    {
        //TODO check if this is working correctly
        if (this.isActive(event.getBlock().getWorld()))
        {
            boolean first = true;
            for (Block block : event.getBlocks())
            {
                if (block.getType().equals(AIR)) continue;
                BlockState oldState = block.getState();
                BlockState movedTo = block.getRelative(event.getDirection()).getState();
                movedTo.setType(oldState.getType());
                movedTo.setRawData(oldState.getRawData());
                if (first)
                {
                    first = false;
                    //TODO perhaps newState not Air but orientated pistonextension
                    this.logBlockChange(oldState.getLocation(),null,BlockData.of(oldState), AIR, null); // pushing
                }
                this.logBlockChange(null,movedTo.getBlock().getState(),movedTo, null); // pushed
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event)
    {
        //TODO check if this is working correctly
        if (event.isSticky() && this.isActive(event.getBlock().getWorld()))
        {
            BlockState retractingBlock = event.getRetractLocation().getBlock().getState();
            if (retractingBlock.getType().equals(AIR)) return;
            BlockState retractedBlock = event.getBlock().getRelative(event.getDirection()).getState();
            retractedBlock.setType(retractingBlock.getType());
            retractedBlock.setRawData(retractingBlock.getRawData());
            //TODO perhaps newState not Air but orientated pistonextension
            this.logBlockChange(retractingBlock.getLocation(), null, BlockData.of(retractingBlock), AIR, null); // pulling
            this.logBlockChange(null,retractedBlock.getBlock().getState(),retractedBlock, null); // pulled
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            user.sendTranslated("%s&6%s&a got moved away by a Piston &6%d times%s&a!",
                    time,logEntry.getOldBlock(),logEntry.getAttached().size(),loc);
        }
        else
        {
            user.sendTranslated("%s&6%s&a got moved away by a Piston%s&a!",
                                time,logEntry.getOldBlock(),loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BLOCK_SHIFT_enable;
    }
}
