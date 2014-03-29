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
package de.cubeisland.engine.log.action.newaction.block.player;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.engine.core.module.Module;
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
    public PlayerBucketListener(Module module)
    {
        super(module);
    }

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
        if (blockState.getType() == Material.WATER || blockState.getType() == Material.STATIONARY_WATER || blockState
            .getType() == Material.LAVA || blockState.getType() == Material.STATIONARY_LAVA)
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
