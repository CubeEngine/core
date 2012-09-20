package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.BasicUserManager;
import de.cubeisland.cubeengine.basics.Basics;
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
    UserManager cuManager = CubeEngine.getUserManager();
    
    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = cuManager.getUser(event.getPlayer());
        if (user.getAttachment(BasicUser.class).hasUnlimitedItems())
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount()+1);
        }
    }
}
