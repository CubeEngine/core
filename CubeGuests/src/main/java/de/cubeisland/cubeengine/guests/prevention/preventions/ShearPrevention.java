package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;

/**
 * Prevents shearing
 *
 * @author Phillip Schichtel
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
