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
package de.cubeisland.engine.log.action.logaction.block;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;
import static org.bukkit.Material.AIR;

/**
 * Blocks burning
 * <p>Events: {@link BlockBurnEvent}</p>
 * <p>External Actions:
 * {@link BlockActionType#logAttachedBlocks BlockBreak and HangingBreak} when attached Blocks will fall
 * {@link BlockActionType#logFallingBlocks BlockFall} when relative Blocks will fall
 */
public class BlockBurn extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(FIRE, BLOCK, ENVIRONEMENT));
    }

    @Override
    public String getName()
    {
        return "block-burn";
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
        this.logAttachedBlocks(event.getBlock().getState(), null);
        this.logFallingBlocks(event.getBlock().getState(), null);
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated(MessageType.POSITIVE, "{}{name#block} went up into flames{}", time, logEntry.getOldBlock(), loc);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.BLOCK_BURN_enable;
    }
}
