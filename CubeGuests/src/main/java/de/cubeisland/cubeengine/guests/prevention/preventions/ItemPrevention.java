package de.cubeisland.cubeengine.guests.prevention.preventions;

import de.cubeisland.cubeengine.guests.Guests;
import de.cubeisland.cubeengine.guests.prevention.FilteredItemPrevention;
import java.util.EnumSet;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Prevents item usage.
 */
public class ItemPrevention extends FilteredItemPrevention
{
    public ItemPrevention(Guests guests)
    {
        super("item", guests);
        setIgnoreBlocks(true);
        setFilterItems(EnumSet.of(Material.FLINT_AND_STEEL));
        setFilterMode(FilterMode.BLACKLIST);
    }

    @Override
    public String getConfigHeader()
    {
        return super.getConfigHeader() + "\nBlocks will not be count as items!\n";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void interact(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL)
        {
            final ItemStack itemInHand = event.getItem();
            if (itemInHand != null)
            {
                if (prevent(event, event.getPlayer(), event.getItem().getType()))
                {
                    event.setUseInteractedBlock(Result.DENY);
                    event.setUseItemInHand(Result.DENY);
                }
            }
        }
    }
}
