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
package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import java.util.EnumSet;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockForm;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.ENVIRONEMENT;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.FLOW;
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
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, ENVIRONEMENT, FLOW);
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
            user.sendTranslated("%s&aLava occupied this block &6%d times%s&a!",time,amount,loc);
        }
        else
        {
            user.sendTranslated("%s&aLava occupied the block%s&a!",time,loc);
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).LAVA_FLOW_enable;
    }
}
