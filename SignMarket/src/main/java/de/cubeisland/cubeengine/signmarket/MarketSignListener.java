package de.cubeisland.cubeengine.signmarket;

import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign)
        {
            MarketSign marketSign = this.module.getMarketSignFactory().getSignAt(event.getClickedBlock().getLocation());
            if (marketSign == null)
                return;
            User user = this.module.getUserManager().getExactUser(event.getPlayer());
            if (marketSign.executeAction(user, event.getAction()))
            {
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }
}
