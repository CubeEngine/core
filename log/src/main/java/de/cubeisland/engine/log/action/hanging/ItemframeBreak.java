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
package de.cubeisland.engine.log.action.hanging;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ENTITY_HANGING;

/**
 * Represents a player breaking an itemframe
 */
public class ItemframeBreak extends HangingBreak
{
    public ItemStack item; // TODO item format

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ItemframeBreak && this.player.equals(((ItemframeBreak)action).player)
            && ((ItemframeBreak)action).item == null && this.item == null;
    }

    @Override
    public String translateAction(User user)
    {
        // TODO indirect
        if (this.hasAttached())
        {
            int amount = this.getAttached().size() + 1;
            return user.getTranslation(POSITIVE, "{amount} empty {text:itemframes} got removed by {user}", amount,
                                       this.player.name);
        }
        if (this.item == null)
        {
            return user.getTranslation(POSITIVE, "{text:itemframe} got removed by {user}", this.player.name);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} broke an {text:item-frame} containing {name#item}",
                                       this.player.name, this.item.getType().name());
        }
    }
}
