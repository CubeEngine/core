package de.cubeisland.cubeengine.log.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ContainerListener implements Listener
{

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event){

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event)
    {

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    //TODO figure out how it works... in 1.5
    public void onInventoryClick(InventoryClickEvent event)
    {

    }
}
