package de.cubeisland.engine.log.action.newaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.destroy.PlayerBucketFill;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerLavaBucketPlace;
import de.cubeisland.engine.log.action.newaction.block.player.place.PlayerWaterBucketPlace;
import de.cubeisland.engine.log.action.newaction.player.MilkFill;

import static org.bukkit.Material.*;

/**
 * A Listener for PlayerBucket Actions
 * <p>Events:
 * {@link PlayerBucketEmptyEvent}
 * {@link PlayerBucketFillEvent}
 * <p>Actions:
 * {@link PlayerWaterBucketPlace}
 * {@link PlayerLavaBucketPlace}
 * {@link PlayerBucketFill}
 * <p>External Actions:
 * {@link MilkFill}
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        BlockState blockState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (blockState.getType() == Material.WATER || blockState.getType() == Material.STATIONARY_WATER
            || blockState.getType() == Material.LAVA || blockState.getType() == Material.STATIONARY_LAVA)
        {
            PlayerBucketFill action = this.newAction(PlayerBucketFill.class, event.getPlayer().getWorld());
            if (action != null)
            {
                action.setPlayer(event.getPlayer());
                action.setLocation(blockState.getLocation());
                action.setOldBlock(blockState);
                action.setNewBlock(AIR);
                this.logAction(action);
            }
        }
        else // TODO better check
        {
            // milk
            MilkFill action = this.newAction(MilkFill.class, event.getPlayer().getWorld());
            if (action != null)
            {
                action.setPlayer(event.getPlayer());
                action.setLocation(blockState.getLocation());
                this.logAction(action);
            }
        }
    }

}
