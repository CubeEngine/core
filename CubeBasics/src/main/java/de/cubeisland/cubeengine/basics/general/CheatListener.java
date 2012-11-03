package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class CheatListener implements Listener
{
    private Basics basics;

    public CheatListener(Basics basics)
    {
        this.basics = basics;
    }

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = basics.getUserManager().getExactUser(event.getPlayer());
        if (user.getAttribute(basics,"unlimitedItems") != null)
        {
            if (user.getAttribute(basics,"unlimitedItems"))
            {
                ItemStack itemInHand = event.getPlayer().getItemInHand();
                itemInHand.setAmount(itemInHand.getAmount() + 1);
            }
        }
    }
}