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

import org.bukkit.entity.Item;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.entity.ActionEntityBlock.EntitySection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents a player picking up an item
 */
public class ItemPickup extends ActionItem
{
    public EntitySection entity;

    public ItemPickup()
    {
        super("pickup", ITEM);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        if (this.hasAttached())
        {
            if (this.getAttached().size() == 1 && this.getAttached().get(
                0) instanceof ItemDrop) // TODO other ItemDrop
            {
                // Drop / Pickup Pair
                return false;
            }
        }
        else if (action instanceof ItemDrop) // TODO other ItemDrop
        {
            // Drop / Pickup Pair
            return this.entity.equals(((ItemDrop)action).entity);
        }
        return action instanceof ItemPickup && this.player.equals(((ItemPickup)action).player)
            && ((ItemPickup)action).item.isSimilar(this.item);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = item.getAmount();
        if (this.hasAttached())
        {
            if (this.getAttached().size() == 1 && this.getAttached().get(0) instanceof ItemDrop)
            {
                ItemDrop dropAction = (ItemDrop)this.getAttached().get(0);
                // Drop / Pickup Pair
                return user.getTranslation(POSITIVE,
                                           "{user} dropped {name#item} x{amount} and {user} picked it up again",
                                           dropAction.player.name, this.item.getType().name(), amount,
                                           this.player.name);
            }
            for (BaseAction action : this.getAttached())
            {
                amount += ((ItemPickup)action).item.getAmount();
            }
        }
        return user.getTranslation(POSITIVE, "{user} picked up {name#item} x{amount}", this.player.name,
                                   this.item.getType().name(), amount);
    }

    public void setItem(Item item)
    {
        this.setItemstack(item.getItemStack());
        this.entity = new EntitySection(item);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.item.pickup;
    }
}
