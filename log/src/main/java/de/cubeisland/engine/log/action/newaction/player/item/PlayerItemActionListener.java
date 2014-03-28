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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.log.action.newaction.player.PlayerLogListener;

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
public class PlayerItemActionListener extends PlayerLogListener
{
    public PlayerItemActionListener(Module module)
    {
        super(module);
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
                action.setItem(event.getRecipe().getResult());
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
            action.setItem(event.getItem());
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
}
