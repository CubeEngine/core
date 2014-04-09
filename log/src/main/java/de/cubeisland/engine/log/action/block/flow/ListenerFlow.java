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
package de.cubeisland.engine.log.action.block.flow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.LogListener;
import de.cubeisland.engine.log.action.block.ActionBlock;

import static de.cubeisland.engine.core.util.BlockUtil.*;
import static org.bukkit.Material.*;
import static org.bukkit.block.BlockFace.DOWN;
import static org.bukkit.block.BlockFace.UP;

/**
 * A Listener for {@link ActionFlow}
 * <p>Events:
 * {@link BlockFromToEvent}
 * <p>All Actions:
 * {@link LavaBreak}
 * {@link LavaFlow}
 * {@link WaterBreak}
 * {@link WaterFlow}
 * {@link WaterForm}
 * {@link LavaWaterForm}
 */
public class ListenerFlow extends LogListener
{
    public ListenerFlow(Log module)
    {
        super(module, LavaBreak.class, LavaFlow.class, WaterBreak.class, WaterFlow.class, WaterForm.class,
              LavaWaterForm.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event)
    {
        // TODO CE-117 Lava currently impossible to log sometimes
        BlockState toState = event.getToBlock().getState();
        BlockState fromState = event.getBlock().getState();
        if (fromState.getType() == LAVA || fromState.getType() == STATIONARY_LAVA)
        {
            handleLavaFlow(event, toState, fromState);
        }
        else if (fromState.getType() == WATER || fromState.getType() == STATIONARY_WATER)
        {
            handleWaterFlow(event, toState, fromState);
        }
    }

    @SuppressWarnings("deprecation")
    private void handleWaterFlow(BlockFromToEvent event, BlockState toState, BlockState fromState)
    {
        Material material = toState.getType();
        if (material == WATER || material == STATIONARY_WATER)
        {
            this.waterFlowFormSource(event.getToBlock());
        }
        else if (material == LAVA || material == STATIONARY_LAVA && toState.getRawData() < 3)
        {
            this.log(LavaWaterForm.class, toState, COBBLESTONE);
        }
        else if (material == AIR || isNonFluidProofBlock(material))
        {
            this.waterFlowForm(event);
            this.waterFlowFormSource(event.getToBlock());
            this.logFlow(material == AIR ? WaterFlow.class : WaterBreak.class, toState, WATER, fromState);
        }
    }

    @SuppressWarnings("deprecation")
    private void handleLavaFlow(BlockFromToEvent event, BlockState toState, BlockState fromState)
    {
        Material material = toState.getType();
        if (material == WATER || material == STATIONARY_WATER)
        {
            if (event.getFace() == DOWN)
            {
                this.log(LavaWaterForm.class, toState, STONE);
                return;
            }
            this.log(LavaWaterForm.class, toState, COBBLESTONE);
        }
        else if (material == AIR || isNonFluidProofBlock(material))
        {
            if (isSurroundedByWater(event.getToBlock()))
            {
                if (material == REDSTONE_WIRE)
                {
                    this.log(LavaWaterForm.class, toState, OBSIDIAN);
                    return;
                }
                if (fromState.getRawData() <= 4) // TODO this seems odd
                {
                    this.log(LavaWaterForm.class, toState, COBBLESTONE);
                    return;
                }
            }
            this.logFlow(material == AIR ? LavaFlow.class : LavaBreak.class, toState, LAVA, fromState);
        }
    }

    private void waterFlowFormSource(Block toBlock)
    {
        int sources = 0;
        for (BlockFace face : DIRECTIONS)
        {
            Block nearBlock = toBlock.getRelative(face);
            if (nearBlock.getType() == STATIONARY_WATER)
            {
                sources++;
            }
        }
        if (sources >= 2)
        {
            // created new source block
            this.log(WaterForm.class, toBlock.getState(), STATIONARY_WATER);
        }
        // else only changing water-level do not log
    }

    private void waterFlowForm(BlockFromToEvent event)
    {
        for (final BlockFace face : BLOCK_FACES)
        {
            if (face == UP)
            {
                continue;
            }
            final Block nearBlock = event.getToBlock().getRelative(face);
            if (nearBlock.getType() == LAVA && nearBlock.getState().getRawData() <= 4)
            {
                this.log(LavaWaterForm.class, nearBlock.getState(), COBBLESTONE);
            }
            if (nearBlock.getType() == STATIONARY_LAVA)
            {
                this.log(LavaWaterForm.class, nearBlock.getState(), OBSIDIAN);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void logFlow(Class<? extends ActionFlow> clazz, BlockState state, Material to, BlockState from)
    {
        ActionBlock action = this.newAction(clazz, state.getWorld());
        if (action != null)
        {
            BlockState newState = state.getBlock().getState();
            newState.setType(to);
            newState.setRawData((byte)(from.getRawData() + 1));

            action.setLocation(state.getLocation());
            action.setOldBlock(state);
            action.setNewBlock(newState);
            this.logAction(action);
        }
    }

    private void log(Class<? extends ActionBlock> clazz, BlockState state, Material to)
    {
        ActionBlock action = this.newAction(clazz, state.getWorld());
        if (action != null)
        {
            action.setLocation(state.getLocation());
            action.setOldBlock(state);
            action.setNewBlock(to);
            this.logAction(action);
        }
    }
}
