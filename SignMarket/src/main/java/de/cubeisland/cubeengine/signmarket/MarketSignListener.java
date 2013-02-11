package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class MarketSignListener implements Listener
{

    private final Signmarket module;

    public MarketSignListener(Signmarket module) {
        this.module = module;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
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
                BlockState block =event.getPlayer().getTargetBlock(null, 6).getState();
                if (block instanceof Sign)
                {
                    MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(block.getLocation());
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
    }
}
