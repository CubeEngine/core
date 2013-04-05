package de.cubeisland.cubeengine.log.action.logaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType.BlockData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * Container-ActionType for emptying Buckets.
 * <p>Events: {@link PlayerBucketEmptyEvent}</p>
 * <p>External Actions: {@link LavaBucket} when placing lava,
 * {@link WaterBucket} when placing water
 */
public class BucketEmpty extends LogActionType
{
    public BucketEmpty(Log module)
    {
        super(module, -1, "BUCKET_EMPTY");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (event.getBucket().equals(Material.WATER_BUCKET))
        {
            WaterBucket waterBucket = this.manager.getActionType(WaterBucket.class);
            if (waterBucket.isActive(state.getWorld()))
            {
                waterBucket.logBlockChange(state.getLocation(), event.getPlayer(), BlockData.of(state), Material.WATER, null);
            }
        }
        else if (event.getBucket().equals(Material.LAVA_BUCKET))
        {
            LavaBucket lavaBucket = this.manager.getActionType(LavaBucket.class);
            if (lavaBucket.isActive(state.getWorld()))
            {
                lavaBucket.logBlockChange(state.getLocation(), event.getPlayer(), BlockData.of(state), Material.LAVA, null);
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        throw new UnsupportedOperationException();
    }
}
