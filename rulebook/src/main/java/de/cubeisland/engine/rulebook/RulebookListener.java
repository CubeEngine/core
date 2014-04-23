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
package de.cubeisland.engine.rulebook;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.bukkit.AfterJoinEvent;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.rulebook.bookManagement.RulebookManager;

class RulebookListener implements Listener
{

    private final Rulebook module;
    private final RulebookManager rulebookManager;

    public RulebookListener(Rulebook module)
    {
        this.module = module;
        this.rulebookManager = module.getRuleBookManager();
    }

    @EventHandler
    public void onPlayerLanguageReceived(AfterJoinEvent event)
    {
        User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if(!user.hasPlayedBefore() && !this.rulebookManager.getLocales().isEmpty())
        {
            Locale locale = user.getLocale();
            if (!this.rulebookManager.contains(locale))
            {
                locale = this.module.getCore().getI18n().getDefaultLanguage().getLocale();
                if (!this.rulebookManager.contains(locale))
                {
                    locale = this.rulebookManager.getLocales().iterator().next();
                }
            }
            
            ItemStack hand = user.getItemInHand();
            user.setItemInHand(this.rulebookManager.getBook(locale));

            if(hand != null && hand.getType() != Material.AIR)
            {
                for(ItemStack item : user.getInventory().addItem(hand).values())
                {
                    user.getWorld().dropItemNaturally(user.getLocation(), item);
                }
            }
        }
    }
}
