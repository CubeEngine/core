package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

/**
 * Prevents shearing.
 */
public class ShearPrevention extends Prevention
{
    public ShearPrevention(Guests guests)
    {
        super("shear", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void shearEntity(PlayerShearEntityEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
