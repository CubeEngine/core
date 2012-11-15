package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTameEvent;

/**
 * Prevents taming.
 */
public class TamePrevention extends Prevention
{

    public TamePrevention(Guests guests)
    {
        super("tame", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void tame(EntityTameEvent event)
    {
        if (event.getOwner() instanceof Player)
        {
            prevent(event, (Player)event.getOwner());
        }
    }
}
