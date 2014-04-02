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
package de.cubeisland.engine.log.action.newaction.player.entity.hanging.destroy;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.entity.hanging.PlayerHangingActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player breaking an hanging entity
 * <p>SubActions:
 * {@link PlayerPaintingBreak}
 * {@link PlayerItemFrameBreak}
 */
public class PlayerHangingBreak extends PlayerHangingActionType
{
    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerPaintingBreak && this.player.equals(((PlayerPaintingBreak)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{text:One} {name#hanging} got removed by {user}",
                                    "{3:amount} {name#hanging} got removed by {user}", this.hanging.name(),
                                    this.player.name, count);
    }

    public void setCause(ActionTypeBase action)
    {
        // TODO reference
    }

    @Override
    public ActionTypeCategory getCategory()
    {
        return ActionTypeCategory.ENTITY_HANGING;
    }

    @Override
    public String getName()
    {
        return "break";
    }
}
