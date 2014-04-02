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

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.entity.EntityBlockActionType.EntitySection;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType.PlayerSection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionTypeCategory.DEATH;

/**
 * Represents something killing a LivingEntity
 */
public class KillAction extends ActionTypeBase<DeathListener>
{
    public PlayerSection playerKiller = null;
    public EntitySection entityKiller = null;
    public DamageCause otherKiller = null;
    public boolean projectile = false;

    // TODO item in hand

    public boolean isPlayerKiller()
    {
        return playerKiller != null;
    }

    public boolean isEntityKiller()
    {
        return entityKiller != null;
    }

    public boolean isOtherKiller()
    {
        return otherKiller != null;
    }

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        if (action instanceof KillAction)
        {
            if (this.isPlayerKiller() && ((KillAction)action).isPlayerKiller())
            {
                return this.playerKiller.equals(((KillAction)action).playerKiller);
            }
            if (this.isEntityKiller() && ((KillAction)action).isEntityKiller())
            {
                return this.entityKiller.isSameType(((KillAction)action).entityKiller);
            }
            if (this.isOtherKiller() && ((KillAction)action).isOtherKiller())
            {
                return this.otherKiller == ((KillAction)action).otherKiller;
            }
        }
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        if (this.isPlayerKiller())
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{user} killed an entity",
                                        "{user} killed {amount} entities",
                                        this.playerKiller.name, count);
        }
        if (this.isEntityKiller())
        {
            return user.getTranslationN(POSITIVE, count,
                                        "{name#entity} killed an entity",
                                        "{name#entity} killed {amount} entities",
                                        this.entityKiller.name(), count);
        }
        if (this.isOtherKiller())
        {
            return user.getTranslationN(POSITIVE, count, "{name#cause} killed an entity",
                                        "{name#cause} killed {amount} entities", this.otherKiller.name(), count);
        }
        return "INVALID KILLTYPE!";
    }

    @Override
    public ActionTypeCategory getCategory()
    {
        return DEATH;
    }

    @Override
    public String getName()
    {
        return "kill";
    }
}
