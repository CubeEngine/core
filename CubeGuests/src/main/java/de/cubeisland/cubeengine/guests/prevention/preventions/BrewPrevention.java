package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.Prevention;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * Prevents brewing
 *
 * @author Phillip Schichtel
 */
public class BrewPrevention extends Prevention
{
    public BrewPrevention(Guests guests)
    {
        super("brew", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void open(InventoryOpenEvent event)
    {
        if (event.getInventory().getType() == InventoryType.BREWING)
        {
            if (event.getPlayer() instanceof Player)
            {
                prevent(event, (Player)event.getPlayer());
            }
        }
    }
}
