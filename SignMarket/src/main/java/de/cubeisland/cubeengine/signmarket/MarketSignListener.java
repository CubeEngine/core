package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
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
                return;
            marketSign.updateSign();
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            if (marketSign.executeAction(user, event.getAction()))
            {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
        else if (event.getAction().equals(Action.RIGHT_CLICK_AIR))
        {
            if (event.getPlayer().getItemInHand() != null && event.getPlayer().getItemInHand().getTypeId() != 0)
            {
                BlockState lastSignFound = getTargettedSign(event.getPlayer());
                if (lastSignFound == null)
                    return;
                MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(lastSignFound.getLocation());
                if (marketSign == null)
                    return;
                marketSign.updateSign();
                User user = this.module.getUserManager().getExactUser(event.getPlayer());
                if (marketSign.executeAction(user, Action.RIGHT_CLICK_BLOCK))
                {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                    event.setCancelled(true);
                }
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
