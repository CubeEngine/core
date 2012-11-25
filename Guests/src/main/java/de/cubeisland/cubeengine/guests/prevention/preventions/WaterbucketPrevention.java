package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

/**
 * Prevents water bucket usage.
 */
public class WaterbucketPrevention extends Prevention
{
    public WaterbucketPrevention(Guests guests)
    {
        super("waterbucket", guests);
        setEnableByDefault(true);
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\nThis prevention works, even though the client shows that water was placed!\n";
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void empty(PlayerBucketEmptyEvent event)
    {
        if (event.getBucket() == Material.WATER_BUCKET)
        {
            prevent(event, event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void fill(PlayerBucketFillEvent event)
    {
        if (event.getItemStack().getType() == Material.WATER_BUCKET)
        {
            prevent(event, event.getPlayer());
        }
    }
}
