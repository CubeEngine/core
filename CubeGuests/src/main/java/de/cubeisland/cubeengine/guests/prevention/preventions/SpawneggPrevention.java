package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.prevention.FilteredEntityPrevention;
import de.cubeisland.cubeengine.guests.Guests;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents spawnegg usage
 *
 * @author Phillip Schichtel
 */
public class SpawneggPrevention extends FilteredEntityPrevention
{
    public SpawneggPrevention(Guests guests)
    {
        super("spawnegg", guests, true);
        setEnablePunishing(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interact(PlayerInteractEvent event)
    {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
        {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.MONSTER_EGG)
            {
                if (prevent(event, event.getPlayer(), EntityType.fromId(item.getData().getData())))
                {
                    event.setUseInteractedBlock(Event.Result.DENY);
                    event.setUseItemInHand(Event.Result.DENY);
                }
            }
        }
    }
}
