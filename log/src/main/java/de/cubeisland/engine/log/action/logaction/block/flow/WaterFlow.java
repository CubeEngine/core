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
package de.cubeisland.engine.log.action.logaction.block.flow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.block.BlockForm;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.engine.core.util.BlockUtil.DIRECTIONS;
import static de.cubeisland.engine.log.action.ActionTypeCategory.*;
import static org.bukkit.Material.*;

/**
 * Water-flow
 * <p>Events: {@link FlowActionType}</p>
 * <p>External Actions:
 * {@link WaterFlow},  {@link BlockForm}
 */
public class WaterFlow extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, ENVIRONEMENT, FLOW));
    }

    @Override
    public String getName()
    {
        return "water-flow";
    }

    public void logWaterFlow(BlockFromToEvent event, BlockState toBlock, BlockState newToBlock, BlockState fromBlock)
    {
        if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
        {
            int sources = 0;
            for (BlockFace face : DIRECTIONS)
            {
                Block nearBlock = event.getToBlock().getRelative(face);
                if (nearBlock.getType().equals(Material.STATIONARY_WATER) && nearBlock.getData() == 0)
                {
                    sources++;
                }
            }
            if (sources >= 2) // created new source block
            {
                this.logBlockForm(toBlock,newToBlock,WATER);
            }// else only changing water-level do not log
            return;
        }
        if (newToBlock.getType().equals(Material.LAVA) || newToBlock.getType().equals(Material.STATIONARY_LAVA) && newToBlock.getRawData() <= 2)
        {
            this.logBlockForm(toBlock,newToBlock,COBBLESTONE);
            return;
        }
        for (final BlockFace face : BLOCK_FACES)
        {
            if (face.equals(BlockFace.UP))continue;
            final Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType().equals(Material.LAVA) && nearBlock.getState().getRawData() <=4 || nearBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                BlockState oldNearBlock = nearBlock.getState();
                BlockState newNearBlock = nearBlock.getState();
                this.logBlockForm(oldNearBlock,newNearBlock,nearBlock.getData() == 0 ? OBSIDIAN : COBBLESTONE);
            }
        }
        newToBlock.setType(Material.WATER);
        newToBlock.setRawData((byte)(fromBlock.getRawData() + 1));
        if (toBlock.getType().equals(AIR))
        {
            if (this.isActive(toBlock.getWorld()))
            {
                this.logBlockChange(null,toBlock,newToBlock,null);
            }
        }
        else
        {
            this.logWaterBreak(toBlock,newToBlock);
        }
    }

    private void logWaterBreak(BlockState toBlock, BlockState newToBlock)
    {
        WaterBreak waterBreak = this.manager.getActionType(WaterBreak.class);
        if (waterBreak.isActive(toBlock.getWorld()))
        {
            waterBreak.logBlockChange(null,toBlock,newToBlock,null);
        }
    }

    private void logBlockForm(BlockState toBlock, BlockState newToBlock, Material newType)
    {
        BlockForm blockForm = this.manager.getActionType(BlockForm.class);
        if (blockForm.isActive(toBlock.getWorld()))
        {
            newToBlock.setType(newType);
            newToBlock.setRawData((byte)0);
            blockForm.logBlockChange(null,toBlock,newToBlock,null);
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size();
            user.sendTranslated(MessageType.POSITIVE, "{}Water flooded {amount}x the block{}", time, amount, loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}Water flooded the block{}", time, loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.flow.WATER_FLOW_enable;
    }
}
