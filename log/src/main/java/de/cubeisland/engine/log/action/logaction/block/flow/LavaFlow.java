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
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.BlockUtil;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.block.BlockForm;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;
import static org.bukkit.Material.*;

/**
 * Lava-flow
 * <p>Events: {@link FlowActionType}</p>
 * <p>External Actions:
 * {@link LavaBreak}, {@link BlockForm}
 */
public class LavaFlow extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, ENVIRONEMENT, FLOW));
    }

    @Override
    public String getName()
    {
        return "lava-flow";
    }


    public void logLavaFlow(BlockFromToEvent event, BlockState toBlock, BlockState newToBlock, BlockState fromBlock)
    {

        if (toBlock.getType().equals(Material.WATER) || toBlock.getType().equals(Material.STATIONARY_WATER))
        {
            if (event.getFace().equals(BlockFace.DOWN))
            {
                this.logBlockForm(toBlock, newToBlock, STONE);
            }
            else
            {
                this.logBlockForm(toBlock, newToBlock, COBBLESTONE);
            }
            return;
        }
        if (toBlock.getType().equals(Material.REDSTONE_WIRE) && BlockUtil.isSurroundedByWater(event.getToBlock()))
        {
            this.logBlockForm(toBlock, newToBlock, OBSIDIAN);
            return;
        }
        if (fromBlock.getRawData() <= 4 && BlockUtil.isSurroundedByWater(event.getToBlock()))
        {
            this.logBlockForm(toBlock, newToBlock, COBBLESTONE);
            return;
        }
        newToBlock.setType(Material.LAVA);
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
            if (toBlock.getType().equals(Material.LAVA) || toBlock.getType().equals(Material.STATIONARY_LAVA))
            {
                return; // changing lava-level do not log
            }
            this.logLavaBreak(toBlock,newToBlock);
        }
    }

    private void logLavaBreak(BlockState toBlock, BlockState newToBlock)
    {
        LavaBreak lavaBreak = this.manager.getActionType(LavaBreak.class);
        if (lavaBreak.isActive(toBlock.getWorld()))
        {
            lavaBreak.logBlockChange(null,toBlock,newToBlock,null);
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
            user.sendTranslated(MessageType.POSITIVE, "{}Lava occupied this block {amount} times{}", time, amount, loc);
        }
        else
        {
            user.sendTranslated(MessageType.POSITIVE, "{}Lava occupied the block{}", time, loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.flow.LAVA_FLOW_enable;
    }
}
