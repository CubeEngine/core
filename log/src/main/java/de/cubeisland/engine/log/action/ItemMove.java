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
package de.cubeisland.engine.log.action;

import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.player.item.container.ContainerType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ITEM;

/**
 * Represents items transferred by hoppers or droppers
 */
public class ItemMove extends BaseAction
{
    public ItemStack item;
    public BlockFace direction;
    public ContainerType fromContainer;
    public ContainerType toContainer;

    public ItemMove()
    {
        super("move", ITEM);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ItemMove && this.direction == ((ItemMove)action).direction && this.coord.equals(
            action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "Moved {1:name#item} {name#direction}",
                                    "Moved {2:amount} items {name#direction}", this.direction.name(),
                                    this.item.getType().name(), count);
        // TODO from invType to invType
        // TODO separate for entity containers with uuid to be able to track position
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.container.move;
    }
}
