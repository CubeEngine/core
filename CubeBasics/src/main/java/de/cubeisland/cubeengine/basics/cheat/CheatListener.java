package de.cubeisland.cubeengine.basics.cheat;

import de.cubeisland.cubeengine.basics.BasicUserManager;
import de.cubeisland.cubeengine.basics.CubeBasics;
import de.cubeisland.cubeengine.core.CubeCore;
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
    UserManager cuManager = CubeCore.getInstance().getUserManager();
    BasicUserManager buManager = CubeBasics.getInstance().getBuManager();
    
    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = cuManager.getUser(event.getPlayer());
        if (buManager.getBasicUser(user).hasUnlimitedItems())
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount()+1);
        }
    }
}
