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
package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.newaction.BaseAction;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player accessing an {@link org.bukkit.inventory.InventoryHolder}
 */
public class ContainerAccess extends PlayerBlockAction<PlayerBlockInteractListener>
{
    // TODO no rollback/redo

    // return this.lm.getConfig(world).container.CONTAINER_ACCESS_enable;

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ContainerAccess && this.player.equals(((PlayerBlockAction)action).player)
            && this.coord.equals(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} looked into a {name#container}",
                                    "{user} looked into {2:amount} {name#container}", this.player.name,
                                    this.oldBlock.name(), count);
    }

    @Override
    public ActionCategory getCategory()
    {
        return ActionCategory.USE;
    }

    @Override
    public String getName()
    {
        return "container";
    }
}
