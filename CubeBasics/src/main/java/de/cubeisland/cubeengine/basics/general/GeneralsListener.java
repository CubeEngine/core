package de.cubeisland.cubeengine.basics.general;

import de.cubeisland.cubeengine.basics.BasicUser;
import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.core.bukkit.AfterJoinEvent;
import de.cubeisland.cubeengine.core.user.User;
import net.minecraft.server.EntityPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GeneralsListener implements Listener
{
    private Basics basics;

    public GeneralsListener(Basics basics)
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

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(event.getPlayer());
        if (!BasicsPerm.COMMAND_GOD_KEEP.isAuthorized(event.getPlayer()))
        {
            bUser.godMode = false;
        }
        this.basics.getBasicUserManager().update(bUser); //update godmode
    }

    @EventHandler
    public void onAfterJoin(AfterJoinEvent event)
    {
        User user = basics.getUserManager().getExactUser(event.getPlayer());
        int amount = basics.getMailManager().countMail(user);
        if (amount > 0)
        {
            user.sendMessage("basics", "You have %d new mails!\nUse /mail read to display them.", amount);
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        if (bUser.godMode == true)
        {
            ((CraftPlayer)user.getPlayer()).getHandle().abilities.isInvulnerable = true;
        }
    }
}