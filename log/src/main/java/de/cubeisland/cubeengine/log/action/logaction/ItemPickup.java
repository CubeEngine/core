package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;

public class ItemPickup extends SimpleLogActionType
{
    public ItemPickup(Log module)
    {
        super(module, 0x85, "item-pickup");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if (this.isActive(event.getPlayer().getWorld()))
        {
            String itemData = new ItemData(event.getItem().getItemStack()).serialize(this.om);
            this.logSimple(event.getPlayer(),itemData);
        }
    }
}
