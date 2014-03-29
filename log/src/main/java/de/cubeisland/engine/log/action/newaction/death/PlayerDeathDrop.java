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

import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.bigdata.Reference;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.item.PlayerItemDrop;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player dropping items on death
 */
public class PlayerDeathDrop extends ActionTypeBase<DeathListener>
{
    public Reference<PlayerDeath> death;
    public ItemStack item;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerDeathDrop && this.death != null && ((PlayerDeathDrop)action).death != null && this.death
            .equals(((PlayerDeathDrop)action).death);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = this.item.getAmount();
        if (this.hasAttached())
        {
            for (ActionTypeBase action : this.getAttached())
            {
                amount += ((PlayerItemDrop)action).item.getAmount();
            }
        }
        return user.getTranslation(POSITIVE, "{user} dropped {name#item} x{amount} upon death",
                                   this.death.fetch(PlayerDeath.class).killed.name, this.item.getType().name(), amount);
    }
}
