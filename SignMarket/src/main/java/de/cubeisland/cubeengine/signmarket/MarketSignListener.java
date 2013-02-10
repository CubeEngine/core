package de.cubeisland.cubeengine.signmarket;

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
        if (event.getClickedBlock() != null)
        {

        }
    }
}
