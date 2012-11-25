package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBedEnterEvent;

/**
 * Prevents bed usage.
 */
public class BedPrevention extends Prevention
{
    public BedPrevention(Guests guests)
    {
        super("bed", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void bedEnter(PlayerBedEnterEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
