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

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.log.action.logaction.ActionTypeContainer;

import static de.cubeisland.cubeengine.core.bukkit.BlockUtil.isNonFluidProofBlock;
import static org.bukkit.Material.AIR;

/**
 * Container-ActionType water-lava flows
 * <p>Events: {@link BlockFromToEvent}</p>
 * <p>External Actions:
 * {@link LavaFlow},
 * {@link WaterFlow}
 */
public class FlowActionType extends ActionTypeContainer
{
    public FlowActionType()
    {
        super("FLOW");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(final BlockFromToEvent event)
    {
        // TODO CE-344 Lava currently impossible to log sometimes
        BlockState toBlock = event.getToBlock().getState();
        final boolean canFlow = toBlock.getType().equals(AIR) || isNonFluidProofBlock(toBlock.getType());
        if (!canFlow)
        {
            return;
        }
        BlockState fromBlock = event.getBlock().getState();
        BlockState newToBlock = event.getToBlock().getState();
        Material fromMat = event.getBlock().getType();
        if (fromMat.equals(Material.LAVA) || fromMat.equals(Material.STATIONARY_LAVA))
        {
            LavaFlow lavaFlow = this.manager.getActionType(LavaFlow.class);
            lavaFlow.logLavaFlow(event,toBlock,newToBlock,fromBlock);
        }
        else if (fromMat.equals(Material.WATER) || fromMat.equals(Material.STATIONARY_WATER))
        {
            WaterFlow waterFlow = this.manager.getActionType(WaterFlow.class);
            waterFlow.logWaterFlow(event,toBlock,newToBlock,fromBlock);
        }
    }
}
