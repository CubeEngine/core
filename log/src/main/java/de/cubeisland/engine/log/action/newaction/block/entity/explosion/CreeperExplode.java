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
 * Represents a creeper blowing up blocks
 */
public class CreeperExplode extends ExplosionActionType
{
    // return "creeper-explode";
    // return this.lm.getConfig(world).block.explode.CREEPER_EXPLODE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof CreeperExplode && this.oldBlock
            .equals(((CreeperExplode)action).oldBlock) && this.entity
            .equals(((CreeperExplode)action).entity) && ((this.player == null && ((CreeperExplode)action).player == null) || (this.player != null && this.player
            .equals(((CreeperExplode)action).player)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.player == null)
        {
            return user
                .getTranslationN(POSITIVE, count, "A Creeper-Explosion wrecked {name#block}", "A Creeper-Explosion wrecked {1:amount}x {name#block}", this.oldBlock
                    .name(), count);
        }
        return user
            .getTranslationN(POSITIVE, count, "{user} let a Creeper detonate and destroy {name#block}", "{user} let a Creeper detonate and destroy {amount}x {name#block}", this.player.name, this.oldBlock
                .name(), count);
    }
}
