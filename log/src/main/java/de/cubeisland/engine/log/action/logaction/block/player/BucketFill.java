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
package de.cubeisland.engine.log.action.logaction.block.player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.BlockActionType;
import de.cubeisland.engine.log.action.logaction.interact.MilkFill;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;
import static org.bukkit.Material.AIR;


/**
 * Filling buckets with lava or water
 * <p>Events: {@link PlayerBucketFillEvent}</p>
 * <p>External Actions: {@link MilkFill} when filling a milk bucket
 */
public class BucketFill extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BUCKET, BLOCK, PLAYER));
    }
    @Override
    public String getName()
    {
        return "bucket-fill";
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
                milkFill.logSimple(event.getBlockClicked().getLocation(),event.getPlayer(),null);
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
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled {amount} buckets with lava{}", time, logEntry.getCauserUser().getDisplayName(), amount, loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.WATER) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_WATER))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled {amount} buckets with water{}", time, logEntry.getCauserUser().getDisplayName(), amount, loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled {amount} buckets with some random fluids{}!", time, logEntry.getCauserUser().getDisplayName(), amount, loc);
            }
        }
        else
        {
            if (logEntry.getOldBlock().material.equals(Material.LAVA) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_LAVA))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled a bucket with lava{}", time, logEntry.getCauserUser().getDisplayName(), loc);
            }
            else if (logEntry.getOldBlock().material.equals(Material.WATER) ||
                logEntry.getOldBlock().material.equals(Material.STATIONARY_WATER))
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled a bucket with water{}", time, logEntry.getCauserUser().getDisplayName(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{user} filled a bucket with some random fluids{}", time, logEntry.getCauserUser().getDisplayName(), loc);
            }
        }
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.bucket.BUCKET_FILL_enable;
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return super.isSimilar(logEntry, other);
        /*
        if ((logEntry.newBlock == other.newBlock || logEntry.newBlock.equals(other.newBlock))
            && logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.additional == other.additional) // additional
        {
            if (logEntry.block.equals(other.block))
            {
                return nearTimeFrame(logEntry,other);
            }
        }*/
        // TODO attach Water flooded the block to filled bucket with water
        // Player filled water from an infinite source
    }
}
