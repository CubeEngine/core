package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents jukebox usage
 *
 * @author Phillip Schichtel
 */
public class JukeboxPrevention extends Prevention
{
    public JukeboxPrevention(Guests guests)
    {
        super("jukebox", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            if (event.getClickedBlock().getType() == Material.JUKEBOX)
            {
                prevent(event, event.getPlayer());
            }
        }
    }
}
