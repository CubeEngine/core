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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.BasicsAttachment;
import de.cubeisland.engine.basics.BasicsUser;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.core.bukkit.AfterJoinEvent;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.roles.RoleAppliedEvent;

public class GeneralsListener implements Listener
{
    private final Basics module;

    public GeneralsListener(Basics basics)
    {
        this.module = basics;
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            BasicsUserEntity bUser = this.module.getBasicsUser((Player)event.getEntity()).getbUEntity();
            if (bUser.getGodmode())
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void blockplace(final BlockPlaceEvent event)
    {
        User user = module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (user.get(BasicsAttachment.class).hasUnlimitedItems())
        {
            ItemStack itemInHand = event.getPlayer().getItemInHand();
            itemInHand.setAmount(itemInHand.getAmount() + 1);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        BasicsUserEntity bUser = this.module.getBasicsUser(event.getPlayer()).getbUEntity();
        if (!module.perms().COMMAND_GOD_KEEP.isAuthorized(event.getPlayer()))
        {
            bUser.setGodmode(false);
        }
        bUser.update();
        if (!module.perms().COMMAND_GAMEMODE_KEEP.isAuthorized(event.getPlayer()))
        {
            event.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode()); // reset gamemode to default on the server
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event)
    {
        BasicsUserEntity bUser = this.module.getBasicsUser(event.getPlayer()).getbUEntity();
        if (!module.perms().COMMAND_GOD_KEEP.isAuthorized(event.getPlayer()))
        {
            bUser.setGodmode(false);
        }
        bUser.update();
        if (!module.perms().COMMAND_GAMEMODE_KEEP.isAuthorized(event.getPlayer()))
        {
            event.getPlayer().setGameMode(Bukkit.getServer().getDefaultGameMode()); // reset gamemode to default on the server
        }
    }

    @EventHandler
    public void onAfterJoin(AfterJoinEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        BasicsUser bUser = this.module.getBasicsUser(user);
        int amount = bUser.countMail();
        if (amount > 0)
        {
            user.sendTranslated(MessageType.POSITIVE, "You have {amount} new mails!", amount);
            user.sendTranslated(MessageType.NEUTRAL, "Use {text:/mail read} to display them.");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        BasicsUser bUser = this.module.getBasicsUser(user);
        if (bUser.getbUEntity().getGodmode())
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
                User clicker = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
                clicker.sendTranslated(MessageType.POSITIVE, "This {name#entity} belongs to {tamer}!", Match.entity().getNameFor(event.getRightClicked().getType()), tamed.getOwner());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(RoleAppliedEvent event)
    {
        String meta = event.getAttachment().getCurrentMetadataString("tablist-prefix");
        if (meta != null)
        {
            String colored = ChatFormat.parseFormats(meta) + event.getUser().getDisplayName();
            if (colored.length() > 16)
            {
                colored = colored.substring(0,16);
            }
            event.getUser().setPlayerListName(colored);
        }
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event)
    {
        if (this.module.getConfiguration().preventOverstackedItems && !module.perms().OVERSTACKED_ANVIL_AND_BREWING.isAuthorized(event.getWhoClicked()))
        {

            if (event.getView().getTopInventory() instanceof AnvilInventory
                || event.getView().getTopInventory() instanceof BrewerInventory)
            {
                boolean topClick = event.getRawSlot() < event.getView().getTopInventory().getSize();
                switch (event.getAction())
                {
                case PLACE_ALL:
                case PLACE_SOME:
                    if (!topClick) return;
                    if (event.getCursor().getAmount() > event.getCursor().getMaxStackSize())
                    {
                        event.setCancelled(true);
                    }
                    break;
                case MOVE_TO_OTHER_INVENTORY:
                    if (topClick) return;
                    if (event.getCurrentItem().getAmount() > event.getCurrentItem().getMaxStackSize())
                    {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
