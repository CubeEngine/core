package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.Prevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

/**
 * Prevents sign changing
 *
 * @author Phillip Schichtel
 */
public class ChangesignPrevention extends Prevention
{
    public ChangesignPrevention(Guests guests)
    {
        super("changesign", guests);
        setEnableByDefault(true);
        setEnablePunishing(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void change(SignChangeEvent event)
    {
        prevent(event, event.getPlayer());
    }
}
