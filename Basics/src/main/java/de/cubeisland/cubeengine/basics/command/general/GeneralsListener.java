package de.cubeisland.cubeengine.basics.command.general;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.basics.BasicsAttachment;
import de.cubeisland.cubeengine.basics.BasicsPerm;
import de.cubeisland.cubeengine.basics.storage.BasicUser;
import de.cubeisland.cubeengine.core.bukkit.AfterJoinEvent;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
    public void onDamage(final EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            BasicUser bUser = this.basics.getBasicUserManager().getBasicUser((Player)event.getEntity());
            if (bUser.godMode)
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = basics.getUserManager().getExactUser(event.getPlayer());
        if (user.get(BasicsAttachment.class).hasUnlimitedItems())
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount() + 1);
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
            user.sendTranslated("&aYou have &6%d &anew mails!\n&eUse &6/mail read &eto display them.", amount);
        }
        BasicUser bUser = this.basics.getBasicUserManager().getBasicUser(user);
        if (bUser.godMode == true)
        {
            user.setInvulnerable(true);
        }
    }

    @EventHandler
    public void onInteractWithTamed(PlayerInteractEntityEvent event)
    {
        if (event.getRightClicked() != null && event.getRightClicked() instanceof Tameable)
        {
            Tameable tamed = (Tameable) event.getRightClicked();
            if (tamed.getOwner() != null && !event.getPlayer().equals(tamed.getOwner()))
            {
                User clicker = this.basics.getUserManager().getExactUser(event.getPlayer());
                clicker.sendTranslated("&aThis &6%s &abelongs to &2%s&a!", Match.entity().getNameFor(event.getRightClicked().getType()), tamed.getOwner().getName());
            }
        }
    }
}
