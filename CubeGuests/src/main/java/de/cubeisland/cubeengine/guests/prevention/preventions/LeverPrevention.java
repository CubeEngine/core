package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents lever usage
 *
 * @author Phillip Schichtel
 */
public class LeverPrevention extends Prevention
{
    public LeverPrevention(Guests guests)
    {
        super("lever", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event)
    {
        final Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.LEVER)
            {
                prevent(event, event.getPlayer());
            }
        }
    }
}
