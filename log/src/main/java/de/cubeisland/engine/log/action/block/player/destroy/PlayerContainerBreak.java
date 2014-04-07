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
package de.cubeisland.engine.log.action.block.player.destroy;

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking a container
 */
public class PlayerContainerBreak extends PlayerBlockBreak
{
    public ItemStack[] contents;

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return super.translateAction(user);
        }
        int amount = 0;
        for (ItemStack content : this.contents)
        {
            if (content != null)
            {
                amount++;
            }
        }
        if (amount == 0)
        {
            return user.getTranslation(POSITIVE, "{user} broke an empty {name#container}", this.player.name,
                                       this.oldBlock.name());
        }
        return user.getTranslationN(POSITIVE, amount, "{user} broke {name#container} with a single stack of items",
                                    "{user} broke {name#container} with {amount} stacks of items", this.player.name, this.oldBlock.name(),
                                    amount);
    }

    public void setContents(ItemStack[] contents)
    {
        this.contents = contents;
    }

    // TODO custom rollback/redo
}
