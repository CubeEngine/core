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
package de.cubeisland.engine.log.action.newaction.player.entity.hanging;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player removing an item from an item-frame
 */
public class PlayerItemFrameItemRemove extends PlayerHangingActionType
{
    // return "remove-item";
    // return this.lm.getConfig(world).ITEM_REMOVE_FROM_FRAME;

    public ItemStack item;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerItemFrameItemRemove
            && this.player.equals(((PlayerItemFrameItemRemove)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "{user} removed {name#item} from an itemframe",
                                    "{user} removed {2:amount} items from itemframes",
                                    this.player.name, this.item.getType().name(), count);
    }

    // TODO redo/rollback
}
