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
package de.cubeisland.engine.log.action.newaction.player.item;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.newaction.player.PlayerLogListener;

import static org.bukkit.Material.FIREWORK;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

/**
 * A Listener for Player Actions with Items
 * <p>Events:
 * {@link CraftItemEvent}
 * {@link EnchantItemEvent}
 * {@link PlayerDropItemEvent}
 * {@link PlayerPickupItemEvent}
 * <p>Actions:
 * {@link CraftItem}
 * {@link EnchantItem}
 * {@link PlayerItemDrop}
 * {@link PlayerItemPickup}
 */
public class PlayerItemListener extends PlayerLogListener
{
    public PlayerItemListener(Log module)
    {
        super(module, CraftItem.class, EnchantItem.class, PlayerItemDrop.class, PlayerItemPickup.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event)
    {
        if (event.getRecipe().getResult() == null)
        {
            return;
        }
        if (event.getWhoClicked() instanceof Player)
        {
            CraftItem action = this.newAction(CraftItem.class, event.getWhoClicked().getWorld());
            if (action != null)
            {
                this.setPlayerAndLocation((Player)event.getWhoClicked(), action);
                action.setItemstack(event.getRecipe().getResult());
                this.logAction(action);
            }
        }
        else
        {
            // TODO missing action entity craft item
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnchant(EnchantItemEvent event)
    {
        EnchantItem action = this.newAction(EnchantItem.class, event.getEnchanter().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getEnchanter(), action);
            if (event.getEnchantBlock() != null)
            {
                action.setLocation(event.getEnchantBlock().getLocation());
            }
            action.setItemstack(event.getItem());
            action.setEnchants(event.getEnchantsToAdd());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event)
    {
        PlayerItemDrop action = this.newAction(PlayerItemDrop.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setItem(event.getItemDrop());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        PlayerItemPickup action = this.newAction(PlayerItemPickup.class, event.getPlayer().getWorld());
        if (action != null)
        {
            this.setPlayerAndLocation(event.getPlayer(), action);
            action.setItem(event.getItem());
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFireworkLaunch(PlayerInteractEvent event)
    {
        if (event.getAction() == RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getType() == FIREWORK)
        {
            FireworkUse action = this.newAction(FireworkUse.class, event.getPlayer().getWorld());
            action.setPlayer(event.getPlayer());
            action.setLocation(event.getClickedBlock().getRelative(event.getBlockFace()).getLocation());
            // TODO item
            this.logAction(action);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        PotionSplash action = this.newAction(PotionSplash.class, event.getPotion().getWorld());
        if (action != null)
        {
            if (event.getPotion().getShooter() instanceof Player)
            {
                action.setPlayer((Player)event.getPotion().getShooter());
                action.setLocation(event.getPotion().getLocation());
                // TODO item etc.
                this.logAction(action);
            }
            // TODO other shooter
        }
    }
}
