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
package de.cubeisland.engine.log.action.newaction.block.entity.explosion;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents the explosion an enderdragon makes when flying through blocks
 */
public class EnderdragonExplode extends ExplosionActionType
{
    // return "enderdragon-explode";
    // return this.lm.getConfig(world).block.explode.ENDERDRAGON_EXPLODE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof EnderdragonExplode && this.oldBlock
            .equals(((EnderdragonExplode)action).oldBlock) && this.entity
            .equals(((EnderdragonExplode)action).entity) && ((this.player == null && ((EnderdragonExplode)action).player == null) || (this.player != null && this.player
            .equals(((EnderdragonExplode)action).player)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.player == null)
        {
            return user
                .getTranslationN(POSITIVE, count, "An enderdragon changed the integrity of {name#block}", "An enderdragon changed the integrity of {amount}x {name#block}", this.oldBlock
                    .name(), count);
        }
        return user
            .getTranslationN(POSITIVE, count, "An enderdragon attacking {user} changed the integrity of {name#block}", "An enderdragon attacking {user} changed the integrity of {2:amount}x {name#block}", this.player.name, this.oldBlock
                .name(), count);
    }
}
