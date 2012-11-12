package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredItemPrevention;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;

/**
 * Prevents placing blocks.
 */
public class PlaceblockPrevention extends FilteredItemPrevention
{
    public PlaceblockPrevention(Guests guests)
    {
        super("placeblock", guests);
        setEnableByDefault(true);
        setEnablePunishing(true);
        setFilterMode(FilterMode.NONE);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void place(BlockPlaceEvent event)
    {
        prevent(event, event.getPlayer(), event.getBlockPlaced().getType());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void place(PaintingPlaceEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
