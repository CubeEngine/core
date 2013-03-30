package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Prevents pressureplate usage.
 */
public class PressureplatePrevention extends Prevention
{
    public PressureplatePrevention(Guests guests)
    {
        super("pressureplate", guests);
        setThrottleDelay(3);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void pressure(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.PHYSICAL)
        {
            final Material material = event.getClickedBlock().getType();
            if (material == Material.STONE_PLATE || material == Material.WOOD_PLATE)
            {
                prevent(event, event.getPlayer());
            }
        }
    }
}
