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
 * Represents a wither causing an explosion
 */
public class ExplodeWither extends ExplosionAction
{
    public ExplodeWither()
    {
        super("wither");
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof ExplodeWither && this.oldBlock.equals(((ExplodeWither)action).oldBlock)
            && this.entity.equals(((ExplodeWither)action).entity) && (
            (this.player == null && ((ExplodeWither)action).player == null) || (this.player != null
                && this.player.equals(((ExplodeWither)action).player)));
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.player == null)
        {
            return user.getTranslationN(POSITIVE, count, "{name#block} got destroyed in a Wither-Explosion",
                                        "{1:amount}x {name#block} got destroyed in a Wither-Explosion",
                                        this.oldBlock.name(), count);
        }
        return user.getTranslationN(POSITIVE, count, "A Wither hunting down {user} blasted away {name#block}",
                                    "A Wither hunting down {user} blasted away {2:amount}x {name#block}",
                                    this.player.name, this.oldBlock.name(), count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.block.explode.wither;
    }
}
