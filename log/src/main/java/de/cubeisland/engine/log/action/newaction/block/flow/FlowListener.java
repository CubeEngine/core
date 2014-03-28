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
package de.cubeisland.engine.log.action.newaction.block.flow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;
import de.cubeisland.engine.log.action.newaction.block.BlockForm;

import static de.cubeisland.engine.core.util.BlockUtil.*;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.DOWN;

/**
 * A Listener for BlockFlow Actions
 * <p>Events:
 * {@link BlockFromToEvent}
 * <p>Actions:
 * {@link LavaBreak}
 * {@link LavaFlow}
 * {@link WaterBreak}
 * {@link WaterFlow}
 * {@link WaterSourceCreation}
 * {@link BlockForm}
 */
public class FlowListener extends LogListener
{
    public FlowListener(Module module)
    {
        super(module);
    }

    // TODO TESTING
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event)
    {
        // TODO CE-117 Lava currently impossible to log sometimes
        BlockState toBlock = event.getToBlock().getState();
        BlockState fromBlock = event.getBlock().getState();
        if (fromBlock.getType() == LAVA || fromBlock.getType() == STATIONARY_LAVA)
        {
            if (toBlock.getType() == AIR)
            {
                if (fromBlock.getRawData() <= 4 && isSurroundedByWater(event.getToBlock()))
                {
                    this.log(BlockForm.class, toBlock, COBBLESTONE);
                    return;
                }
                this.logFlow(LavaFlow.class, toBlock, LAVA, fromBlock);
            }
            else if (toBlock.getType() == WATER || toBlock.getType() == STATIONARY_WATER)
            {
                if (event.getFace() == DOWN)
                {
                    this.log(BlockForm.class, toBlock, STONE);
                }
                else
                {
                    this.log(BlockForm.class, toBlock, COBBLESTONE);
                }
            }
            else if (isNonFluidProofBlock(toBlock.getType()))
            {
                if (toBlock.getType() == Material.REDSTONE_WIRE && isSurroundedByWater(event.getToBlock()))
                {
                    this.log(BlockForm.class, toBlock, OBSIDIAN);
                    return;
                }
                if (fromBlock.getRawData() <= 4 && isSurroundedByWater(event.getToBlock()))
                {
                    this.log(BlockForm.class, toBlock, COBBLESTONE);
                    return;
                }
                this.logFlow(LavaBreak.class, toBlock, LAVA, fromBlock);
            }
        }
        else if (fromBlock.getType() == WATER || fromBlock.getType() == STATIONARY_WATER)
        {
            if (toBlock.getType() == AIR)
            {
                this.waterFlowForm(event);
                this.waterFlowFormSource(event, toBlock);
                this.logFlow(WaterFlow.class, toBlock, WATER, fromBlock);
            }
            else if (toBlock.getType() == WATER || toBlock.getType() == STATIONARY_WATER)
            {
                this.waterFlowFormSource(event, toBlock);
            }
            else if (toBlock.getType() == LAVA || toBlock.getType() == STATIONARY_LAVA && toBlock.getRawData() <= 2)
            {
                this.log(BlockForm.class, toBlock, COBBLESTONE);
            }
            else if (isNonFluidProofBlock(toBlock.getType()))
            {
                this.waterFlowForm(event);
                this.waterFlowFormSource(event, toBlock);
                this.logFlow(WaterBreak.class, toBlock, WATER, fromBlock);
            }
        }
    }

    private void waterFlowFormSource(BlockFromToEvent event, BlockState toBlock)
    {
        int sources = 0;
        for (BlockFace face : DIRECTIONS)
        {
            Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType() == Material.STATIONARY_WATER)
            {
                sources++;
            }
        }
        if (sources >= 2) // created new source block
        {
            this.log(WaterSourceCreation.class, toBlock, STATIONARY_WATER);
        }// else only changing water-level do not log
    }

    private void waterFlowForm(BlockFromToEvent event)
    {
        for (final BlockFace face : BLOCK_FACES)
        {
            if (face.equals(BlockFace.UP))continue;
            final Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType() == LAVA && nearBlock.getState().getRawData() <=4)
            {
                this.log(BlockForm.class, nearBlock.getState(), COBBLESTONE);
            }
            if (nearBlock.getType() == STATIONARY_LAVA)
            {
                this.log(BlockForm.class, nearBlock.getState(), OBSIDIAN);
            }
        }
    }

    private void logFlow(Class<? extends BlockActionType> clazz, BlockState state, Material to, BlockState from)
    {
        BlockActionType action = this.newAction(clazz, state.getWorld());
        if (action != null)
        {
            action.setLocation(state.getLocation());
            action.setOldBlock(state);
            BlockState newState = state.getBlock().getState();
            newState.setType(to);
            newState.setRawData((byte)(from.getRawData() + 1));
            action.setNewBlock(newState);
            this.logAction(action);
        }
    }

    private void log(Class<? extends BlockActionType> clazz, BlockState state, Material to)
    {
        BlockActionType action = this.newAction(clazz, state.getWorld());
        if (action != null)
        {
            action.setLocation(state.getLocation());
            action.setOldBlock(state);
            action.setNewBlock(to);
            this.logAction(action);
        }
    }
}
