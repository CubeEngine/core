package de.cubeisland.engine.log.action.newaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerLavaBucketPlace;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerWaterBucketPlace;

import static org.bukkit.Material.LAVA;
import static org.bukkit.Material.WATER;

/**
 * A Listener for PlayerBucket Actions
 * <p>Events:
 * {@link PlayerBucketEmptyEvent}
 * <p>Actions:
 * {@link PlayerWaterBucketPlace}
 * {@link PlayerLavaBucketPlace}
 */
public class PlayerBucketListener extends LogListener
{
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (event.getBucket().equals(Material.WATER_BUCKET))
        {
            PlayerWaterBucketPlace action = this.newAction(PlayerWaterBucketPlace.class, event.getPlayer().getWorld());
            if (action != null)
            {
                action.setPlayer(event.getPlayer());
                action.setLocation(state.getLocation());
                action.setOldBlock(state);
                action.setNewBlock(WATER);
                this.logAction(action);
            }
        }
        else if (event.getBucket().equals(Material.LAVA_BUCKET))
        {
            PlayerLavaBucketPlace action = this.newAction(PlayerLavaBucketPlace.class, event.getPlayer().getWorld());
            if (action != null)
            {
                action.setPlayer(event.getPlayer());
                action.setLocation(state.getLocation());
                action.setOldBlock(state);
                action.setNewBlock(LAVA);
                this.logAction(action);
            }
        }
    }
}
