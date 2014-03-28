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
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player using a lever
 */
public class LeverUse extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return "lever-use";
    // return this.lm.getConfig(world).block.LEVER_USE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof LeverUse
            && this.player.equals(((PlayerBlockActionType)action).player)
            && this.coord.equals(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        // TODO plural
        if ((logEntry.getNewBlock().data & 0x8) == 0x8)
        {
            return user.getTranslation(POSITIVE, "{user} activated the lever",
                                       this.player.name);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} deactivated the lever",
                                       this.player.name);
        }
    }
}
