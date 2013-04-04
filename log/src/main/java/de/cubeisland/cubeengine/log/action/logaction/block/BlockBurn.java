package de.cubeisland.cubeengine.log.action.logaction.block;

import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import de.cubeisland.cubeengine.log.Log;

import static org.bukkit.Material.AIR;

/**
 * Created with IntelliJ IDEA.
 * User: Anselm
 * Date: 04.04.13
 * Time: 20:30
 * To change this template use File | Settings | File Templates.
 */
public class BlockBurn extends BlockActionType
{
    public BlockBurn(Log module)
    {
        super(module, 0x01, "block-burn");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if (this.isActive(event.getBlock().getWorld()))
        {
            BlockState blockState = event.getBlock().getState();
            blockState = this.adjustBlockForDoubleBlocks(blockState); // WOOD_DOOR IRON_DOOR OR BED_BLOCK
            this.logBlockChange(blockState.getLocation(),null,BlockData.of(blockState), AIR, null);
        }
        this.logAttached(event.getBlock().getState(), null);
        this.logFallingBlocks(event.getBlock().getState(), null);
    }
}
