package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * Prevents fishing.
 */
public class FishPrevention extends Prevention
{
    public FishPrevention(Guests guests)
    {
        super("fish", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void fish(PlayerFishEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
