package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

/**
 * Prevents bow usage.
 */
public class BowPrevention extends Prevention
{
    public BowPrevention(Guests guests)
    {
        super("bow", guests);
        setEnableByDefault(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void shootBow(EntityShootBowEvent event)
    {
        final Entity shooter = event.getEntity();
        if (shooter instanceof Player)
        {
            prevent(event, (Player)shooter);
        }
    }
}
