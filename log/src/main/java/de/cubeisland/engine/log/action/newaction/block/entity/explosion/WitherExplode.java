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
 * Represents a wither causing an explosion
 */
public class WitherExplode extends ExplosionActionType
{
    // return "wither-explode";
    // return this.lm.getConfig(world).block.explode.WITHER_EXPLODE_enable;


    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof WitherExplode
            && this.oldBlock.equals(((WitherExplode)action).oldBlock)
            && this.entity.equals(((WitherExplode)action).entity)
            && ((this.player == null && ((WitherExplode)action).player == null)
            || (this.player != null && this.player.equals(((WitherExplode)action).player)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.player == null)
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{name#block} got destroyed in a Wither-Explosion",
                                        "{1:amount}x {name#block} got destroyed in a Wither-Explosion",
                                        this.oldBlock.name(), count);
        }
        return user.getTranslationN(POSITIVE, count,
                                    "A Wither hunting down {user} blasted away {name#block}",
                                    "A Wither hunting down {user} blasted away {2:amount}x {name#block}",
                                    this.player.name, this.oldBlock.name(), count);
    }
}
