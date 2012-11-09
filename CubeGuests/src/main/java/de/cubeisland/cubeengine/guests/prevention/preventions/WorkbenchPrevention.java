package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * Prevents workbench access
 *
 * @author Phillip Schichtel
 */
public class WorkbenchPrevention extends Prevention
{
    public WorkbenchPrevention(Guests guests)
    {
        super("workbench", guests);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void interact(InventoryOpenEvent event)
    {
        if (event.getInventory().getType() == InventoryType.WORKBENCH)
        {
            if (event.getPlayer() instanceof Player)
            {
                prevent(event, (Player)event.getPlayer());
            }
        }
    }
}
