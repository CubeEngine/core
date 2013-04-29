package de.cubeisland.cubeengine.itemrepair;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.itemrepair.repair.RepairBlockManager;
import de.cubeisland.cubeengine.itemrepair.repair.RepairRequest;
import de.cubeisland.cubeengine.itemrepair.repair.blocks.RepairBlock;

public class ItemRepairListener implements Listener
{
    private Itemrepair module;
    private final RepairBlockManager rbm;
    private final Map<String, RepairRequest> repairRequests;

    public ItemRepairListener(Itemrepair module)
    {
        this.module = module;
        this.rbm = module.getRepairBlockManager();
        this.repairRequests = new HashMap<String, RepairRequest>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        this.repairRequests.remove(event.getPlayer().getName());
        this.rbm.removePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
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
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);

        if (!repairBlock.getPermission().isAuthorized(user))
        {
            user.sendTranslated("&cYou are not allowed to use this repair block!");
            return;
        }

        Inventory inventory = repairBlock.getInventory(user);
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            this.cancelRequest(event);
            user.openInventory(inventory);
        }
        else if (event.getAction() == Action.LEFT_CLICK_BLOCK)
        {
            event.setCancelled(true);
            if (this.repairRequests.containsKey(user.getName()))
            {
                RepairRequest request = this.repairRequests.get(user.getName());
                if (request.getRepairBlock() == repairBlock)
                {
                    repairBlock.repair(request);
                    this.repairRequests.remove(user.getName());
                }
            }
            else
            {
                if (!this.repairRequests.containsKey(user.getName()))
                {
                    RepairRequest request = repairBlock.requestRepair(inventory);
                    if (request != null)
                    {
                        this.repairRequests.put(user.getName(), request);
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
            final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer());
            if (this.repairRequests.containsKey(user.getName()))
            {
                user.sendTranslated("&eThe repair has been cancelled!");
                this.repairRequests.remove(user.getName());
                event.setCancelled(true);
            }
        }
    }
}
