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

import org.bukkit.material.Door;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a door
 */
public class DoorUse extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "door-use";
    // return this.lm.getConfig(world).block.DOOR_USE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof DoorUse && this.player.equals(((PlayerBlockActionType)action).player)
            && this.oldBlock == ((DoorUse)action).oldBlock;
    }

    @Override
    public String translateAction(User user)
    {
        // TODO plurals
        @SuppressWarnings("deprecation") boolean open = this.newBlock.as(Door.class).isOpen();
        if (open)
        {
            return user.getTranslation(POSITIVE, "{user} opened the {name#block}", this.player.name,
                                       this.oldBlock.name());
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} closed the {name#block}", this.player.name,
                                       this.oldBlock.name());
        }
    }
}
