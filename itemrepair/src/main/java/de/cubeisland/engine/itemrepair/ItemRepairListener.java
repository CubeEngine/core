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
package de.cubeisland.engine.itemrepair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.itemrepair.repair.RepairBlockManager;
import de.cubeisland.engine.itemrepair.repair.RepairRequest;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock;
import de.cubeisland.engine.itemrepair.repair.blocks.RepairBlock.RepairBlockInventory;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static org.bukkit.event.Event.Result.DENY;
import static org.bukkit.event.block.Action.LEFT_CLICK_BLOCK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class ItemRepairListener implements Listener
{
    private final Itemrepair module;
    private final RepairBlockManager rbm;
    private final Map<UUID, RepairRequest> repairRequests;

    public ItemRepairListener(Itemrepair module)
    {
        this.module = module;
        this.rbm = module.getRepairBlockManager();
        this.repairRequests = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        this.repairRequests.remove(event.getPlayer().getUniqueId());
        this.rbm.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        final Block block = event.getClickedBlock();
        if (block == null)
        {
            return;
        }
        RepairBlock repairBlock = this.rbm.getRepairBlock(block);
        if (repairBlock == null)
        {
            return;
        }
        event.setCancelled(true);
        event.setUseInteractedBlock(DENY);
        event.setUseItemInHand(DENY);

        if (!repairBlock.getPermission().isAuthorized(user))
        {
            user.sendTranslated(NEGATIVE, "You are not allowed to use this repair block!");
            return;
        }

        RepairBlockInventory inventory = repairBlock.getInventory(user);
        
        if (event.getAction() == RIGHT_CLICK_BLOCK)
        {
            this.cancelRequest(event);
            user.openInventory(inventory.inventory);
        }
        else if (event.getAction() == LEFT_CLICK_BLOCK)
        {
            event.setCancelled(true);
            if (this.repairRequests.containsKey(user.getUniqueId()))
            {
                RepairRequest request = this.repairRequests.get(user.getUniqueId());
                if (request.getRepairBlock() == repairBlock)
                {
                    repairBlock.repair(request);
                    this.repairRequests.remove(user.getUniqueId());
                }
            }
            else
            {
                if (!this.repairRequests.containsKey(user.getUniqueId()))
                {
                    RepairRequest request = repairBlock.requestRepair(inventory);
                    if (request != null)
                    {
                        this.repairRequests.put(user.getUniqueId(), request);
                    }
                }
            }
        }
        else
        {
            this.cancelRequest(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCancelRepair(PlayerInteractEvent event)
    {
        this.cancelRequest(event);
    }

    private void cancelRequest(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL)
        {
            final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
            if (this.repairRequests.containsKey(user.getUniqueId()))
            {
                user.sendTranslated(NEUTRAL, "The repair has been cancelled!");
                this.repairRequests.remove(user.getUniqueId());
                event.setCancelled(true);
            }
        }
    }
}
