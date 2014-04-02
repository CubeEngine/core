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
package de.cubeisland.engine.log.action.newaction.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * Represents a player picking up an xp-orb
 */
public class XpPickup extends PlayerActionType<PlayerActionListener>
{
    // return this.lm.getConfig(world).XP_PICKUP_enable;

    private int exp;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof XpPickup && this.player.equals(((XpPickup)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        int amount = this.exp;
        if (this.hasAttached())
        {
            for (ActionTypeBase action : this.getAttached())
            {
                amount += ((XpPickup)action).exp;
            }
        }
        return user.getTranslation(POSITIVE, "{user} earned {amount} experience", this.player.name, amount);
    }

    public void setExp(int exp)
    {
        this.exp = exp;
    }

    @Override
    public ActionTypeCategory getCategory()
    {
        return PLAYER;
    }

    @Override
    public String getName()
    {
        return "xp";
    }
}
