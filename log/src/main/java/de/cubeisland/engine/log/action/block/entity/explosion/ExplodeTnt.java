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
package de.cubeisland.engine.log.action.block.entity.explosion;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a primed tnt exploding
 */
public class ExplodeTnt extends ExplosionAction
{
    public ExplodeTnt()
    {
        super("tnt");
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ExplodeTnt && this.oldBlock.equals(((ExplodeTnt)action).oldBlock)
            && this.entity.equals(((ExplodeTnt)action).entity) && (
            (this.player == null && ((ExplodeTnt)action).player == null) || (this.player != null && this.player.equals(
                ((ExplodeTnt)action).player)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.player == null)
        {
            return user.getTranslationN(POSITIVE, count, "A TNT-Explosion got rid of {name#block}",
                                        "A TNT-Explosion got rid of {1:amount}x {name#block}", this.oldBlock.name(),
                                        count);
        }
        return user.getTranslationN(POSITIVE, count, "A TNT-Explosion induced by {user} got rid of {name#block}",
                                    "A TNT-Explosion induced by {user} got rid of {2:amount}x {name#block}",
                                    this.player.name, this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.explode.tnt;
    }
}

