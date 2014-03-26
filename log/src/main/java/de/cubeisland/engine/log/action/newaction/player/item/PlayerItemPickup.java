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

import java.util.UUID;

import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player picking up an item
 */
public class PlayerItemPickup extends PlayerActionType<PlayerItemActionListener>
{
    // return "item-pickup";
    // return this.lm.getConfig(world).ITEM_PICKUP_enable;

    public ItemStack item; // TODO item format
    public UUID entityUUID;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        if (this.hasAttached())
        {
            if (this.getAttached().size() == 1 && this.getAttached().get(0) instanceof PlayerItemDrop) // TODO other ItemDrop
            {
                // Drop / Pickup Pair
                return false;
            }
        }
        else if (action instanceof PlayerItemDrop) // TODO other ItemDrop
        {
            // Drop / Pickup Pair
            return ((PlayerItemDrop)action).entityUUID.equals(this.entityUUID);
        }
        return action instanceof PlayerItemPickup && ((PlayerItemPickup)action).playerUUID
            .equals(this.playerUUID) && ((PlayerItemPickup)action).item.isSimilar(this.item);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = item.getAmount();
        if (this.hasAttached())
        {

            if (this.getAttached().size() == 1 && this.getAttached().get(0) instanceof PlayerItemDrop)
            {
                PlayerItemDrop dropAction = (PlayerItemDrop)this.getAttached().get(0);
                // Drop / Pickup Pair
                return user.getTranslation(POSITIVE, "{user} dropped {name#item} x{amount} and {user} picked it up again",
                                          dropAction.playerName, this.item.getType().name(), amount, this.playerName);
            }
            for (ActionTypeBase action : this.getAttached())
            {
                amount += ((PlayerItemPickup)action).item.getAmount();
            }
        }
        return user.getTranslation(POSITIVE, "{user} picked up {name#item} x{amount}",
                                   this.playerName, this.item.getType().name(), amount);
    }

    public void setItem(Item item)
    {
        this.item = item.getItemStack();
        this.entityUUID = item.getUniqueId();
    }
}
