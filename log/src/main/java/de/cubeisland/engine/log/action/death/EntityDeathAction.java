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
package de.cubeisland.engine.log.action.death;

import org.bukkit.entity.Entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.entity.ActionEntityBlock.EntitySection;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.DEATH;

/**
 * Represents an Entity dying
 * <p>Sub Actions:
 * {@link DeathBoss}
 * {@link DeathPet}
 * {@link DeathAnimal}
 * {@link DeathNpc}
 * {@link DeathMonster}
 * {@link DeathOther}
 */
public abstract class EntityDeathAction extends ActionDeath
{
    public EntitySection killed;

    protected EntityDeathAction(String name)
    {
        super(name, DEATH);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof EntityDeathAction && this.killed.isSameType(((EntityDeathAction)action).killed)
            && this.killer != null && ((ActionDeath)action).killer != null && this.killer.fetch(
            DeathKill.class).canAttach(((ActionDeath)action).killer.fetch(DeathKill.class));
    }

    @Override
    public String translateAction(User user)
    {
        DeathKill fetch = this.killer.fetch(DeathKill.class);
        if (fetch.isPlayerKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} {name#entity} got killed by {user}",
                                           this.countAttached(), this.killed.name(), fetch.playerKiller.name);
            }
            return user.getTranslation(POSITIVE, "{name#entity} got killed by {user}", this.killed.name(),
                                       fetch.playerKiller.name);
        }
        if (fetch.isEntityKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} {name#entity} could not escape {name#entity}",
                                           this.countAttached(), this.killed.name(), fetch.entityKiller.name());
            }
            return user.getTranslation(POSITIVE, "{name#entity} could not escape {name#entity}", this.killed.name(),
                                       fetch.entityKiller.name());
        }
        if (fetch.isOtherKiller())
        {
            if (this.hasAttached())
            {
                return user.getTranslation(POSITIVE, "{amount} {name#entity} died of {name#cause}",
                                           this.countAttached(), this.killed.name(), fetch.otherKiller.name());
            }
            return user.getTranslation(POSITIVE, "{name#entity} died of {name#cause}", this.killed.name(),
                                       fetch.otherKiller.name());
        }
        return user.getTranslation(POSITIVE, "{name#entity} died", this.killed.name());
    }

    public void setKilled(Entity entity)
    {
        this.killed = new EntitySection(entity); // TODO rollback info
    }
}
