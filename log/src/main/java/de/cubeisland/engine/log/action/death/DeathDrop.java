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

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.ReferenceHolder;
import de.cubeisland.engine.log.action.player.item.ItemDrop;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.DEATH;

/**
 * Represents an entity dropping items on death
 */
public class DeathDrop extends BaseAction implements ReferenceHolder
{
    public Reference<EntityDeathAction> death;
    public ItemStack item;

    public DeathDrop()
    {
        super("drop", DEATH);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof DeathDrop && this.death != null && ((DeathDrop)action).death != null
            && this.death.equals(((DeathDrop)action).death);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = this.item.getAmount();
        if (this.hasAttached())
        {
            for (BaseAction action : this.getAttached())
            {
                amount += ((ItemDrop)action).item.getAmount();
            }
        }
        return user.getTranslation(POSITIVE, "{name#entity} dropped {name#item} x{amount} upon death", this.death.fetch(
            EntityDeathAction.class).killed.name(), this.item.getType().name(), amount); // TODO this wont work as EntityDeathAction is abstract
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.item.drop_onEntityDeath;
    }
}
