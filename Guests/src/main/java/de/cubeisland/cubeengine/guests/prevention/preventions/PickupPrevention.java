package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredItemPrevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * Prevents picking up items.
 */
public class PickupPrevention extends FilteredItemPrevention
{
    public PickupPrevention(Guests guests)
    {
        super("pickup", guests);
        setThrottleDelay(3);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void pickupItem(PlayerPickupItemEvent event)
    {
        prevent(event, event.getPlayer(), event.getItem().getItemStack().getType());
    }
}
