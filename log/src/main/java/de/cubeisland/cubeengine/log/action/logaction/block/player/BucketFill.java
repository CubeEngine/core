package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.interact.MilkFill;
import de.cubeisland.cubeengine.log.storage.LogEntry;

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

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            if (logEntry.getOldBlock().material.equals(Material.LAVA) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_LAVA))
            {
                user.sendTranslated("%s&2%s &afilled &6%d&a buckets with lava%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),amount,loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.WATER) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_WATER))
            {
                user.sendTranslated("%s&2%s &afilled &6%s&a buckets with water%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),amount,loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &afilled &6%s buckets with some random fluids%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),amount,loc);
            }
        }
        else
        {
            if (logEntry.getOldBlock().material.equals(Material.LAVA) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_LAVA))
            {
                user.sendTranslated("%s&2%s &afilled a bucket with lava%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.WATER) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_WATER))
            {
                user.sendTranslated("%s&2%s &afilled a bucket with water%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &afilled a bucket with some random fluids%s!",
                                    time,logEntry.getCauserUser().getDisplayName(),loc);
            }
        }
    }
}
