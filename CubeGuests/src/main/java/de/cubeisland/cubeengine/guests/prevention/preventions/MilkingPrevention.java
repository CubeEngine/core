package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Prevents milking of cows
 *
 * @author Phillip Schichtel
 */
public class MilkingPrevention extends Prevention
{
    public MilkingPrevention(Guests guests)
    {
        super("milking", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void fill(PlayerBucketFillEvent event)
    {
        if (event.getItemStack().getType() == Material.MILK_BUCKET)
        {
            prevent(event, event.getPlayer());
        }
    }
}
