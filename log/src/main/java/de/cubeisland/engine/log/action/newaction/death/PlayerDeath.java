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
package de.cubeisland.engine.log.action.newaction.death;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType.PlayerSection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a players death
 */
public class PlayerDeath extends DeathAction
{
    // return "player-death";
    // return this.lm.getConfig(world).death.PLAYER_DEATH_enable;

    public PlayerSection killed;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerDeath
            && this.killer != null && ((DeathAction)action).killer != null
            && this.killer.fetch(KillAction.class).canAttach(((DeathAction)action).killer.fetch(KillAction.class));
    }

    @Override
    public String translateAction(User user)
    {
        KillAction fetch = this.killer.fetch(KillAction.class);
        if (fetch.isPlayerKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} players got killed by {user}", this.countAttached(), fetch.playerKiller.name);
            }
            return user.getTranslation(POSITIVE, "{user} got killed by {user}", this.killed.name, fetch.playerKiller.name);
        }
        if (fetch.isEntityKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} players could not escape {name#entity}", this.countAttached(), fetch.entityKiller.name());
            }
            return user.getTranslation(POSITIVE, "{user} could not escape {name#entity}", this.killed.name, fetch.entityKiller.name());
        }
        if (fetch.isOtherKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} players died of {name#cause}", this.countAttached(), fetch.otherKiller.name());
            }
            return user.getTranslation(POSITIVE, "{user} died of {name#cause}", this.killed.name, fetch.otherKiller.name());
        }
        return user.getTranslation(POSITIVE, "{user} died", this.killed.name);
    }

    public void setPlayer(Player player)
    {
        this.killed = new PlayerSection(player);
    }
}
