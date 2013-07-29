/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.basics.command.general;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsPerm;
import de.cubeisland.engine.basics.BasicsUser;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.core.bukkit.AfterJoinEvent;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.matcher.Match;

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
            BasicsUserEntity bUser = this.basics.getBasicsUser((Player)event.getEntity()).getbUEntity();
            if (bUser.isGodMode())
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = basics.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (user.get(BasicsAttachment.class).hasUnlimitedItems())
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount() + 1);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        BasicsUserEntity bUser = this.basics.getBasicsUser(event.getPlayer()).getbUEntity();
        if (!BasicsPerm.COMMAND_GOD_KEEP.isAuthorized(event.getPlayer()))
        {
            bUser.setGodMode(false);
        }
        bUser.update(this.basics.getCore().getDB().getEbeanServer());
        if (!BasicsPerm.COMMAND_GAMEMODE_KEEP.isAuthorized(event.getPlayer()))
        {
            event.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode()); // reset gamemode to default on the server
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event)
    {
        BasicsUserEntity bUser = this.basics.getBasicsUser(event.getPlayer()).getbUEntity();
        if (!BasicsPerm.COMMAND_GOD_KEEP.isAuthorized(event.getPlayer()))
        {
            bUser.setGodMode(false);
        }
        bUser.update(this.basics.getCore().getDB().getEbeanServer());
        if (!BasicsPerm.COMMAND_GAMEMODE_KEEP.isAuthorized(event.getPlayer()))
        {
            event.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode()); // reset gamemode to default on the server
        }
    }

    @EventHandler
    public void onAfterJoin(AfterJoinEvent event)
    {
        User user = this.basics.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        BasicsUser bUser = this.basics.getBasicsUser(user);
        int amount = bUser.countMail();
        if (amount > 0)
        {
            user.sendTranslated("&aYou have &6%d &anew mails!\n&eUse &6/mail read &eto display them.", amount);
        }
        if (bUser.getbUEntity().isGodMode())
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
                User clicker = this.basics.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                clicker.sendTranslated("&aThis &6%s &abelongs to &2%s&a!", Match.entity().getNameFor(event.getRightClicked().getType()),tamed.getOwner().getName());
            }
        }
    }
}
