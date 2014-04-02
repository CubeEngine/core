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
package de.cubeisland.engine.log.action.newaction.block.player.interact;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static org.bukkit.Material.REDSTONE_COMPARATOR_ON;

/**
 * Represents a player changing a comparator state
 */
public class ComparatorChange extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return this.lm.getConfig(world).block.COMPARATPR_CHANGE_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof ComparatorChange && this.player.equals(((PlayerBlockActionType)action).player)
            && this.coord.equals(action.coord);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} changed the comparator state {amount} times", this.player.name,
                                       this.countAttached());
        }
        if (this.newBlock.is(REDSTONE_COMPARATOR_ON))
        {
            return user.getTranslation(POSITIVE, "{user} activated the comparator", this.player.name);
        }
        return user.getTranslation(POSITIVE, "{user} deactivated the comparator", this.player.name);
    }

    @Override
    public ActionTypeCategory getCategory()
    {
        return ActionTypeCategory.USE;
    }

    @Override
    public String getName()
    {
        return "comparator";
    }
}
