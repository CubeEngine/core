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
import java.util.List;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.StructureGrowEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.block.player.PlayerGrow;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.BLOCK;
import static de.cubeisland.engine.log.action.ActionTypeCategory.ENVIRONEMENT;

/**
 * Trees or mushrooms growing
 * <p>Events: {@link StructureGrowEvent}
 * <p>External Actions: {@link PlayerGrow}
 */
public class NaturalGrow extends BlockActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(BLOCK, ENVIRONEMENT));
    }

    @Override
    public String getName()
    {
        return "natural-grow";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        Player player = event.getPlayer();
        BlockActionType actionType;
        if (player == null)
        {
            actionType = this;
        }
        else
        {
            actionType = this.manager.getActionType(PlayerGrow.class);
        }
        this.logGrow(actionType,event.getWorld(),event.getBlocks(),player);
    }

    private void logGrow(BlockActionType actionType, World world, List<BlockState> blocks, Player player)
    {
        if (actionType.isActive(world))
        {
            for (BlockState newBlock : blocks)
            {
                BlockState oldBlock = world.getBlockAt(newBlock.getLocation()).getState();
                if (!(oldBlock.getTypeId() == newBlock.getTypeId() && oldBlock.getRawData() == newBlock.getRawData()))
                {
                    actionType.logBlockChange(player, oldBlock, newBlock, null);
                }
            }
        }
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        if (logEntry.hasAttached())
        {
            int amount = logEntry.getAttached().size()+1;
            user.sendTranslated(MessageType.POSITIVE, "{}{amount}x {name#block} grew naturally{}", time, amount, logEntry.getNewBlock(), loc);
        }
        else
        {
            if (logEntry.hasReplacedBlock())
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#block} grew naturally into {name#block}{}", time, logEntry.getNewBlock(), logEntry.getOldBlock(), loc);
            }
            else
            {
                user.sendTranslated(MessageType.POSITIVE, "{}{name#block} grew naturally{}", time, logEntry.getNewBlock(), loc);
            }
        }
    }


    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).block.grow.NATURAL_GROW_enable;
    }
}
