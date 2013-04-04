package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.interact.MilkFill;

import static org.bukkit.Material.AIR;

public class BucketFill extends BlockActionType
{
    public BucketFill(Log module)
    {
        super(module, 0x08, "bucket-fill");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        BlockState blockState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (blockState.getType().equals(Material.WATER) || blockState.getType().equals(Material.STATIONARY_WATER)
         || blockState.getType().equals(Material.LAVA) || blockState.getType().equals(Material.STATIONARY_LAVA))
        {
            if (this.isActive(blockState.getWorld()))
            {
                this.logBlockChange(blockState.getLocation(),event.getPlayer(),BlockData.of(blockState),AIR,null);
            }
        }
        else // milk
        {
            MilkFill milkFill = this.manager.getActionType(MilkFill.class);
            if (milkFill.isActive(event.getBlockClicked().getWorld()))
            {
                milkFill.queueLog(event.getBlockClicked().getLocation(),event.getPlayer(),
                                  null,null,null,null,null);
            }
        }
    }
}
