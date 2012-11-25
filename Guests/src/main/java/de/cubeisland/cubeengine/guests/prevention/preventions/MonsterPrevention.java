package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredEntityPrevention;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;

/**
 * Prevents targeting by monsters.
 */
public class MonsterPrevention extends FilteredEntityPrevention
{
    public MonsterPrevention(Guests guests)
    {
        super("monster", guests, false);
        setEnableByDefault(true);
        setThrottleDelay(3);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void target(EntityTargetEvent event)
    {
        if (event.getEntity() instanceof Monster)
        {
            final Entity target = event.getTarget();
            if (target instanceof Player)
            {
                prevent(event, (Player)target, event.getEntityType());
            }
        }
    }
}
