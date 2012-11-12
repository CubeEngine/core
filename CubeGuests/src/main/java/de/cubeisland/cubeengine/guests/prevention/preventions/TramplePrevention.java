package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents trampling crops.
 */
public class TramplePrevention extends Prevention
{
    public TramplePrevention(Guests guests)
    {
        super("trample", guests);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL && event.getClickedBlock().getType() == Material.SOIL)
        {
            if (prevent(event, event.getPlayer()))
            {
                event.setUseInteractedBlock(Result.DENY);
            }
        }
    }
}
