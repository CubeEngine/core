package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.FilteredItemPrevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 * Prevents item dropping
 *
 * @author Phillip Schichtel
 */
public class DropPrevention extends FilteredItemPrevention
{
    public DropPrevention(Guests guests)
    {
        super("drop", guests);
        setEnablePunishing(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void dropItem(PlayerDropItemEvent event)
    {
        prevent(event, event.getPlayer(), event.getItemDrop().getItemStack().getType());
    }
}
