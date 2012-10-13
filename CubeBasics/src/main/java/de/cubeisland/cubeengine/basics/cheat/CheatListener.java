package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class CheatListener implements Listener
{
    private UserManager um = CubeEngine.getUserManager();

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = this.um.getUser(event.getPlayer());
        if (user.getAttribute("unlimitedItems"))
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount() + 1);
        }
    }
}