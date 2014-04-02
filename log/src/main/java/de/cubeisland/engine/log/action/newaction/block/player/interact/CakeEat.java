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

import org.bukkit.material.Cake;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.player.PlayerBlockActionType;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player eating a cake
 */
public class CakeEat extends PlayerBlockActionType<PlayerBlockInteractListener>
{
    // return this.lm.getConfig(world).block.CAKE_EAT_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false;
    }

    @Override
    public String translateAction(User user)
    {
        int piecesLeft = this.newBlock.as(Cake.class).getSlicesRemaining();
        if (piecesLeft == 0)
        {
            return user.getTranslation(POSITIVE, "The cake is a lie! Ask {user} he knows it!", this.player.name);
        }
        else
        {
            return user.getTranslation(POSITIVE, "{user} ate a piece of cake", this.player.name);
        }
    }
    @Override
    public ActionTypeCategory getCategory()
    {
        return ActionTypeCategory.USE;
    }

    @Override
    public String getName()
    {
        return "cake";
    }
}
