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
package de.cubeisland.engine.log.action.newaction.block.player.bucket;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.LogListener;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;
import de.cubeisland.engine.log.action.newaction.player.MilkFill;

import static org.bukkit.Material.*;

/**
 * A Listener for PlayerBucket Actions
 * <p>Events:
 * {@link PlayerBucketEmptyEvent}
 * {@link PlayerBucketFillEvent}
 * <p>All Actions:
 * {@link PlayerWaterBucketPlace}
 * {@link PlayerLavaBucketPlace}
 * {@link PlayerBucketFill}
 * <p>External Actions:
 * {@link MilkFill}
 */
public class PlayerBucketListener extends LogListener
{
    public PlayerBucketListener(Log module)
    {
        super(module, PlayerWaterBucketPlace.class, PlayerLavaBucketPlace.class, PlayerBucketFill.class, MilkFill.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event)
    {
        BlockState state = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (event.getBucket() == WATER_BUCKET)
        {
            this.setAndLog(PlayerWaterBucketPlace.class, event.getPlayer(), state, WATER);
        }
        else if (event.getBucket() == LAVA_BUCKET)
        {
            this.setAndLog(PlayerLavaBucketPlace.class, event.getPlayer(), state, LAVA);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event)
    {
        BlockState oldState = event.getBlockClicked().getRelative(event.getBlockFace()).getState();
        if (oldState.getType() == WATER || oldState.getType() == STATIONARY_WATER || oldState.getType() == LAVA
            || oldState.getType() == STATIONARY_LAVA)
        {
            this.setAndLog(PlayerBucketFill.class, event.getPlayer(), oldState, AIR);
        }
        else // TODO better check
        {
            // milk
            MilkFill action = this.newAction(MilkFill.class, event.getPlayer().getWorld());
            if (action != null)
            {
                action.setPlayer(event.getPlayer());
                action.setLocation(oldState.getLocation());
                this.logAction(action);
            }
        }
    }

    private void setAndLog(Class<? extends PlayerBlockAction> clazz, Player player, BlockState oldState,
                           Material newMat)
    {
        PlayerBlockAction action = this.newAction(clazz, player.getWorld());
        if (action != null)
        {
            action.setPlayer(player);
            action.setLocation(oldState.getLocation());
            action.setOldBlock(oldState);
            action.setNewBlock(newMat);
            this.logAction(action);
        }
    }
}
