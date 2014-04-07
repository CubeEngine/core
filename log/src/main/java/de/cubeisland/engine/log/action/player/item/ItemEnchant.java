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
package de.cubeisland.engine.log.action.player.item;

import java.util.Map;

import org.bukkit.enchantments.Enchantment;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents a player enchanting an item
 */
public class ItemEnchant extends ActionItem
{
    public ItemEnchant()
    {
        super("enchant", ITEM);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        // same player
        return action instanceof ItemEnchant && this.player.equals(((ItemEnchant)action).player)
            && ((ItemEnchant)action).item.isSimilar(this.item);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} enchanted {name#item} x{amount}", this.player.name,
                                       this.item.getType().name(), this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} enchanted {name#item}", this.player.name,
                                   this.item.getType().name());
        // TODO list enchantments
        // TODO enchant block used
    }

    public void setEnchants(Map<Enchantment, Integer> enchantsToAdd)
    {
        // TODO
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.item.enchant;
    }
}
