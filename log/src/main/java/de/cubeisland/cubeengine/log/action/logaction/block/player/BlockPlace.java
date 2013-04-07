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
package de.cubeisland.cubeengine.log.action.logaction.block.player;

import java.util.EnumSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockActionType;
import de.cubeisland.cubeengine.log.action.logaction.block.BlockFall;
import de.cubeisland.cubeengine.log.storage.LogEntry;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static de.cubeisland.cubeengine.log.action.ActionType.Category.BLOCK;
import static de.cubeisland.cubeengine.log.action.ActionType.Category.PLAYER;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.DRAGON_EGG;

/**
 * Blocks placed by a player.
 * <p>Events: {@link BlockPlaceEvent}</p>
 * <p>External Actions: {@link BlockBreak} when breaking waterlily by replacing the water below,
 * {@link BlockFall}
 */
public class BlockPlace extends BlockActionType
{
    @Override
    protected EnumSet<Category> getCategories()
    {
        return EnumSet.of(BLOCK, PLAYER);
    }

    @Override
    public String getName()
    {
        return "block-place";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Location location = event.getBlockPlaced().getLocation();
        if (this.isActive(location.getWorld()))
        {
            BlockData oldData = BlockData.of(event.getBlockReplacedState());
            BlockData newData = BlockData.of(event.getBlockPlaced().getState());
            this.logBlockChange(location,event.getPlayer(),oldData,newData,null);
            if (event.getBlockPlaced().getRelative(BlockFace.DOWN).getType().equals(AIR)
            && (newData.material.hasGravity() || newData.material.equals(DRAGON_EGG)))
            {
                BlockFall blockFall = this.manager.getActionType(BlockFall.class);
                if (blockFall.isActive(location.getWorld()))
                {
                    blockFall.preplanBlockFall(location.clone(), event.getPlayer(), this); // TODO this does not seem to work
                }
            }
        }
        if (event.getBlock().getRelative(BlockFace.UP).getType().equals(Material.WATER_LILY)
            && !event.getBlockPlaced().getType().equals(Material.STATIONARY_WATER))
        {
            BlockBreak blockBreak = this.manager.getActionType(BlockBreak.class);
            if (blockBreak.isActive(location.getWorld()))
            {
                BlockState state = event.getBlock().getRelative(BlockFace.UP).getState();
                BlockData oldData = BlockData.of(state);
                ObjectNode json = this.om.createObjectNode();
                json.put("break-cause", this.getID());
                blockBreak.logBlockChange(state.getLocation(),event.getPlayer(),oldData,AIR,json.toString());
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated("%s&2%s &aplaced &6%dx %s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    amount,
                                    logEntry.getNewBlock(),
                                    loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &areplaced &6%dx %s&a with &6%s&a%s&a!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    amount,
                                    logEntry.getOldBlock(),
                                    logEntry.getNewBlock(),
                                    loc);
            }
        }
        else // single
        {
            if (logEntry.getOldBlock().material.equals(Material.AIR))
            {
                user.sendTranslated("%s&2%s &aplaced &6%s&a%s!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getNewBlock(),
                                    loc);
            }
            else
            {
                user.sendTranslated("%s&2%s &areplaced &6%s&a with &6%s&a%s&a!",
                                    time,
                                    logEntry.getCauserUser().getDisplayName(),
                                    logEntry.getOldBlock(),
                                    logEntry.getNewBlock(),
                                    loc);
            }
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).BLOCK_PLACE_enable;
    }
}
