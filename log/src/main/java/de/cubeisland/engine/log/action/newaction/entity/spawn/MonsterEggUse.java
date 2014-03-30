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
package de.cubeisland.engine.log.action.newaction.entity.spawn;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType.PlayerSection;
import de.cubeisland.engine.log.action.newaction.entity.EntityActionType;
import de.cubeisland.engine.log.action.newaction.entity.EntityListener;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player spawning a LivingEntity using a spawnegg
 */
public class MonsterEggUse extends EntityActionType<EntityListener>
{
    //return "monsteregg-use";
    //return this.lm.getConfig(world).MONSTER_EGG_USE_enable;

    public PlayerSection player;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof MonsterEggUse && this.entity.isSameType(((MonsterEggUse)action).entity)
            && this.player.equals(((MonsterEggUse)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} spawned {name#entity} using a spawnegg",
                                    "{user} spawned {name#entity} using a spawnegg {amount} times", this.entity.name(),
                                    count);
    }

    public void setPlayer(Player player)
    {
        this.player = new PlayerSection(player);
    }
}
