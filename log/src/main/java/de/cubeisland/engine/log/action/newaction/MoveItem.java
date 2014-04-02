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
package de.cubeisland.engine.log.action.newaction;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.player.item.container.ContainerType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents items transferred by hoppers or droppers
 */
public class MoveItem extends BaseAction<MoveItemListener>
{
    // return this.lm.getConfig(world).container.ITEM_TRANSFER_enable; // TODO make sure default is false! this can produce millions of logs in a very short time

    public ItemStack item;
    public BlockFace direction;

    public ContainerType fromContainer;
    public ContainerType toContainer;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof MoveItem
            && this.direction == ((MoveItem)action).direction
            && this.coord.equals(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count,
                                    "Moved {1:name#item} {name#direction}",
                                    "Moved {2:count} items {name#direction}",
                                    this.direction.name(), this.item.getType().name(), count);
        // TODO from invType to invType
        // TODO separate for entity containers with uuid to be able to track position
    }

    @Override
    public ActionCategory getCategory()
    {
        return ITEM;
    }

    @Override
    public String getName()
    {
        return "move";
    }
}
