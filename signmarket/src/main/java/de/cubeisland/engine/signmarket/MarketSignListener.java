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
package de.cubeisland.engine.signmarket;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Sign;
import org.bukkit.util.BlockIterator;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.BlockUtil;

public class MarketSignListener implements Listener
{

    private final Signmarket module;

    public MarketSignListener(Signmarket module)
    {
        this.module = module;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getPlayer() == null)
        {
            return;
        }
        if (this.module.getMarketSignFactory().getSignAt(event.getBlock().getLocation()) != null)
        {
            event.setCancelled(true);
            return;
        }
        this.handleBlock(event.getBlock(), event);
    }

    private boolean handleBlock(Block block, Cancellable event)
    {
        for (BlockFace blockFace : BlockUtil.CARDINAL_DIRECTIONS)
        {
            Block relative = block.getRelative(blockFace);
            if (relative.getState().getData() instanceof Sign)
            {
                if (this.module.getMarketSignFactory().getSignAt(relative.getLocation()) != null)
                {
                    Block originalBlock = relative.getRelative(((Sign)relative.getState().getData()).getAttachedFace());
                    if (originalBlock.equals(block))
                    {
                        event.setCancelled(true);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event)
    {
        for (Block block : event.getBlocks())
        {
            if (this.handleBlock(block, event)) return;
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event)
    {
        this.handleBlock(event.getRetractLocation().getBlock(), event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY))
        {
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getState().getData() instanceof Sign)
        {
            MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(event.getClickedBlock().getLocation());
            if (marketSign == null)
            {
                return;
            }
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            marketSign.executeAction(user, event.getAction());
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
            event.getPlayer().updateInventory();
        }
        else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) // when placing a block is not possible -> RIGHT_CLICK_AIR instead of RIGHT_CLICK_BLOCK
        {
            if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() != 0)
            {
                BlockState lastSignFound = getTargettedSign(event.getPlayer());
                if (lastSignFound == null)
                {
                    return;
                }
                MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(lastSignFound.getLocation());
                if (marketSign == null)
                {
                    return;
                }
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
                marketSign.executeAction(user, Action.RIGHT_CLICK_BLOCK);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                marketSign.updateSignText();
                event.getPlayer().updateInventory();
            }
        }
    }

    public static BlockState getTargettedSign(Player player)
    {
        BlockIterator blockIterator = new BlockIterator(player.getWorld(), player.getEyeLocation().toVector(), player.getEyeLocation().getDirection(), 0, 7);
        BlockState lastSignFound = null;
        while (blockIterator.hasNext())
        {
            Block block = blockIterator.next();
            if (block.getState() instanceof Sign)
            {
                lastSignFound = block.getState();
            }
            else if (lastSignFound != null && block.getTypeId() != 0)
            {
                break;
            }
        }
        return lastSignFound;
    }
}
