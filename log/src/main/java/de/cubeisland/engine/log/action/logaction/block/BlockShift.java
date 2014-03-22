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
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.material.PistonExtensionMaterial;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.PISTON_EXTENSION;

/**
 * Blocks moved by piston
 * <p>Events: {@link BlockPistonExtendEvent}, {@link BlockPistonRetractEvent}</p>
 */
public class BlockShift extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK));
    }

    @Override
    public String getName()
    {
        return "block-shift";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(final BlockPistonExtendEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            boolean first = true;
            if (event.getBlocks().isEmpty())
            {
                PistonExtensionMaterial pistonHead = new PistonExtensionMaterial(PISTON_EXTENSION);
                pistonHead.setSticky(event.isSticky());
                pistonHead.setFacingDirection(event.getDirection());
                this.logBlockChange(event.getBlock().getRelative(event.getDirection()).getLocation(),null,AIR,
                                    BlockData.of(pistonHead.getItemType(), pistonHead.getData()), null);
                return;
            }
            for (Block block : event.getBlocks())
            {
                BlockState oldState = block.getState();
                if (first)
                {
                    first = false;
                    PistonExtensionMaterial pistonHead = new PistonExtensionMaterial(PISTON_EXTENSION);
                    pistonHead.setSticky(event.isSticky());
                    pistonHead.setFacingDirection(event.getDirection());
                    this.logBlockChange(oldState.getLocation(),null,BlockData.of(oldState),
                        BlockData.of(pistonHead.getItemType(), pistonHead.getData()), null);
                }
                if (block.getType().equals(AIR)) continue;
                BlockState movedTo = block.getRelative(event.getDirection()).getState();
                movedTo.setType(oldState.getType());
                movedTo.setRawData(oldState.getRawData());
                this.logBlockChange(null,movedTo.getBlock().getState(),movedTo, null);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(final BlockPistonRetractEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            BlockState retractingBlock = event.getRetractLocation().getBlock().getState();
            BlockState retractedBlock = event.getBlock().getRelative(event.getDirection()).getState();
            if (retractingBlock.getType().equals(AIR))
            {
                this.logBlockChange(retractedBlock.getBlock().getLocation(), null,
                                BlockData.of(retractedBlock), AIR, null); // pulled
                return;
            }
            retractedBlock.setType(retractingBlock.getType());
            retractedBlock.setRawData(retractingBlock.getRawData());
            this.logBlockChange(retractingBlock.getLocation(), null, BlockData.of(retractingBlock), AIR, null); // pulling
            this.logBlockChange(null,retractedBlock.getBlock().getState(),retractedBlock, null); // pulled
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        String times = "";
        // TODO redo messages
        if (logEntry.hasAttached())
        {
            times = String.format(user.translate(MessageType.POSITIVE, " {amount} times"), logEntry.getAttached().size() + 1);
        }
        if (logEntry.getOldBlock().material == AIR )
        {
            user.sendTranslated(MessageType.POSITIVE, "{}A piston moved {name#block} in place{input#times}{}", time, logEntry.getNewblock(), times, loc);
        }
        else if (logEntry.getOldBlock().material == PISTON_EXTENSION)
        {
            if (logEntry.getNewBlock().material == AIR)
            {
                user.sendTranslated(MessageType.POSITIVE, "{}A piston retracted{input#times}{}", time, times, loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}A piston retracted a moving {name#block} in place{input#times}{}", time, logEntry.getNewBlock(), times, loc);
            }
        }
        else if (logEntry.getNewBlock().material == PISTON_EXTENSION)
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{name#block} got moved away by a Piston{input#times}{}", time, logEntry.getOldBlock(), times, loc);
        }
        else if (logEntry.getNewBlock().material == AIR)
        {
            user.sendTranslated(MessageType.POSITIVE, "{}{name#block} got retracted by a Piston{input#times}{}", time, logEntry.getOldBlock(), times, loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}A piston moved {name#block} in to replace {input#times}{}", time, logEntry.getOldBlock(), logEntry.getNewblock(), times, loc);
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.BLOCK_SHIFT_enable;
    }
}
