package de.cubeisland.cubeengine.log.action.logaction.block.flow;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;

import de.cubeisland.cubeengine.core.util.BlockUtil;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.storage.ActionType;

import static de.cubeisland.cubeengine.core.bukkit.BlockUtil.isNonFluidProofBlock;
import static de.cubeisland.cubeengine.core.util.BlockUtil.BLOCK_FACES;
import static de.cubeisland.cubeengine.core.util.BlockUtil.DIRECTIONS;
import static de.cubeisland.cubeengine.log.storage.ActionType.*;
import static de.cubeisland.cubeengine.log.storage.ActionType.WATER_BREAK;
import static de.cubeisland.cubeengine.log.storage.ActionType.WATER_FLOW;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.COBBLESTONE;
import static org.bukkit.Material.OBSIDIAN;

public class FlowActionType extends BlockActionType
{
    public FlowActionType(Log module)
    {
        super(module, -1, "FLOW");
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
        ActionType action;
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
