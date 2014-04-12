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
package de.cubeisland.engine.log.action.entityspawn;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.player.ActionPlayerBlock.PlayerSection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.SPAWN;

/**
 * Represents a player spawning a LivingEntity using a spawnegg
 */
public class SpawnEgg extends ActionEntitySpawn
{
    public PlayerSection player;

    public SpawnEgg()
    {
        super("egg", SPAWN);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof SpawnEgg && this.entity.isSameType(((SpawnEgg)action).entity)
            && this.player.equals(((SpawnEgg)action).player);
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
        this.player = new PlayerSection(player); // TODO dispenser
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.spawn.monsterEgg;
    }
}
