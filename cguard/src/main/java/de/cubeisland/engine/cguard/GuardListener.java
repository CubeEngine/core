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
package de.cubeisland.engine.cguard;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import de.cubeisland.engine.cguard.storage.Guard;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;

public class GuardListener implements Listener
{
    private GuardManager manager;
    private Cguard module;

    public GuardListener(Cguard module, GuardManager manager)
    {
        this.module = module;
        this.manager = manager;
        this.module.getCore().getEventManager().registerListener(module, this);
    }

    @EventHandler
    public void onPlayerOpenInventory(InventoryOpenEvent event)
    {
        if (!(event.getPlayer() instanceof Player)) return;
        Guard guard;
        InventoryHolder holder = event.getInventory().getHolder();
        Location holderLoc;
        if (holder instanceof Entity)
        {
            guard = this.manager.getGuardForEntityUID(((Entity)holder).getUniqueId());
            holderLoc = ((Entity)holder).getLocation();
        }
        else
        {
            Location location;
            if (holder instanceof BlockState)
            {
                location = ((BlockState)holder).getLocation();
                holderLoc = ((BlockState)holder).getLocation();
            }
            else if (holder instanceof DoubleChest)
            {
                location = ((BlockState)((DoubleChest)holder).getRightSide()).getLocation();
                holderLoc = ((DoubleChest)holder).getLocation();
            }
            else return;
            guard = this.manager.getGuardAtLocation(location);
        }
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            if (guard.isOwner(user)) return;
            if (user.getItemInHand().getType().equals(Material.ENCHANTED_BOOK) && user.getItemInHand().getItemMeta().getDisplayName().contains("KeyBook"))
            {
                String keyBookName = user.getItemInHand().getItemMeta().getDisplayName();
                try
                {
                    long id = Long.valueOf(keyBookName.substring(keyBookName.indexOf('#')+1, keyBookName.length()));
                    if (guard.getId().equals(id)) // Id matches ?
                    {
                        // Validate book
                        if (keyBookName.startsWith(guard.getColorPass()))
                        {
                            user.sendTranslated("&aAs you approach with your KeyBook the magic lock disappears!");
                            user.playSound(holderLoc, Sound.PISTON_EXTEND, 1, 2);
                            user.playSound(holderLoc, Sound.PISTON_EXTEND, 1, (float)1.5);
                            guard.notifyKeyUsage(user);
                            return;
                        }
                        else
                        {
                            user.sendTranslated("&cYou try to open the container with your KeyBook\n" +
                                                    "but forcefully get pushed away!");
                            ItemStack itemInHand = user.getItemInHand();
                            ItemMeta itemMeta = itemInHand.getItemMeta();
                            itemMeta.setDisplayName(ChatFormat.parseFormats("&4Broken KeyBook"));
                            itemMeta.setLore(Arrays.asList(ChatFormat.parseFormats(user.translate("&eThis KeyBook")),
                                                           ChatFormat.parseFormats(user.translate("&elooks old and")),
                                                           ChatFormat.parseFormats(user.translate("&eused up. It")),
                                                           ChatFormat.parseFormats(user.translate("&ewont let you")),
                                                           ChatFormat.parseFormats(user.translate("&eopen any containers!"))));
                            itemInHand.setItemMeta(itemMeta);
                            itemInHand.setType(Material.PAPER);
                            user.updateInventory();
                            user.playSound(holderLoc, Sound.GHAST_SCREAM, 1, 1);
                            final Vector userDirection = user.getLocation().getDirection();
                            user.damage(1);
                            user.setVelocity(userDirection.multiply(-3));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    else
                    {
                        user.sendTranslated("&eYou try to open the container with your KeyBook but nothing happens!");
                        user.playSound(holderLoc, Sound.BLAZE_HIT, 1, 1);
                        user.playSound(holderLoc, Sound.BLAZE_HIT, 1, (float)0.8);
                    }
                }
                catch (NumberFormatException|IndexOutOfBoundsException ignore) // invalid book / we do not care
                {}
            }
            GuardAttachment guardAttachment = user.get(GuardAttachment.class);
            if (guardAttachment != null && guardAttachment.hasUnlocked(guard))
            {
                return;
            }
            guard.handleInventoryOpen(event, user);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Guard guard = this.manager.getGuardAtLocation(event.getBlock().getLocation());
        if (guard != null)
        {
            User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            guard.handleBlockBreak(event, user);
        }
    }

    // TODO placing chest next to protected chest merge OR prevent
}
