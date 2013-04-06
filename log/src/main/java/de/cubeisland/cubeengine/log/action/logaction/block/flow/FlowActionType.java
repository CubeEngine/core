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
