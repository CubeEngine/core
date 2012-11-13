package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener
{
    private Basics basics;

    public AfkListener(Basics basics)
    {
        this.basics = basics;
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onMove(PlayerMoveEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            User user = CubeEngine.getUserManager().getExactUser((Player)event.getWhoClicked());
            user.setAttribute(basics, "lastAction", System.currentTimeMillis());
        }
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(PlayerInteractEvent event)
    {
        User user = CubeEngine.getUserManager().getExactUser(event.getPlayer());
        user.setAttribute(basics, "lastAction", System.currentTimeMillis());
    }
}
