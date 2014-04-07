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

import org.bukkit.entity.Player;

import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.block.entity.ActionEntityBlock;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock.PlayerSection;

import static de.cubeisland.engine.log.action.ActionCategory.EXPLODE;

/**
 * Represents an Entity exploding
 * <p>SubActions:
 * {@link ExplodeCreeper}
 * {@link ExplodeTnt}
 * {@link ExplodeWither}
 * {@link ExplodeFireball}
 * {@link ExplodeEnderdragon}
 * {@link ExplodeEntity}
 */
public abstract class ExplosionAction extends ActionEntityBlock<ListenerExplode>
{
    public PlayerSection player;

    public void setPlayer(Player player)
    {
        this.player = new PlayerSection(player);
    }

    @Override
    public ActionCategory getCategory()
    {
        return EXPLODE;
    }
}
