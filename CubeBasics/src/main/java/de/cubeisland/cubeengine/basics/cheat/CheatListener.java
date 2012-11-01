package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class CheatListener implements Listener
{
    private Basics module;

    public CheatListener(Basics module)
    {
        this.module = module;
    }

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = module.getUserManager().getExactUser(event.getPlayer());
        if (user.getAttribute("unlimitedItems") != null)
        {
            if (user.getAttribute("unlimitedItems"))
            {
                ItemStack itemInHand = event.getPlayer().getItemInHand();
                itemInHand.setAmount(itemInHand.getAmount() + 1);
            }
        }
    }
}