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
package de.cubeisland.cubeengine.signmarket;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

import de.cubeisland.cubeengine.core.user.User;

public class MarketSignListener implements Listener
{

    private final Signmarket module;

    public MarketSignListener(Signmarket module)
    {
        this.module = module;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.useItemInHand().equals(Event.Result.DENY))
        {
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getState() instanceof Sign)
        {
            MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(event.getClickedBlock().getLocation());
            if (marketSign == null)
            {
                return;
            }
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
            marketSign.executeAction(user, event.getAction());
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
            event.setCancelled(true);
            marketSign.updateSignText();
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
                User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
                marketSign.executeAction(user, Action.RIGHT_CLICK_BLOCK);
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
                marketSign.updateSignText();
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
