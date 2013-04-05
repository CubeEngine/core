package de.cubeisland.cubeengine.log.action.logaction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.ItemData;
import de.cubeisland.cubeengine.log.storage.LogEntry;

/**
 * picking up items
 * <p>Events: {@link PlayerPickupItemEvent}</p>
 */
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

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        int amount;
        if (logEntry.hasAttached())
        {
            amount = 42; //TODO
        }
        else
        {
            amount = logEntry.getItemData().amount;
        }
        user.sendTranslated("%s&2%s&a picked up %d &6%s%s&a!",
                            time,logEntry.getCauserUser().getDisplayName(),
                            amount, logEntry.getItemData(), loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return logEntry.world == other.world
            && logEntry.causer == other.causer
            && logEntry.getItemData().equals(other.getItemData());
    }
}
