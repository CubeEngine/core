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
package de.cubeisland.engine.log.action.newaction.player.item;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.player.PlayerActionType;

/**
 * Represents a player using SplashPotions
 */
public class PotionSplash extends PlayerActionType<PlayerItemListener> // TODO potion item
{
    // return "potion-splash";
    // return this.lm.getConfig(world).POTION_SPLASH_enable;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return false; // TODO
    }

    @Override
    public String translateAction(User user)
    {
        return user.getTranslation(MessageType.POSITIVE, "{user} splashed a potion", this.player.name);
        /*
        user.sendTranslated(MessageType.POSITIVE, "{}{user} used {amount} splash potions {input#effects} onto {amount} entities in total{}", time, logEntry
                .getCauserUser().getName(), logEntry.getAttached().size() + 1, effects, amountAffected, loc);
        user.sendTranslated(MessageType.POSITIVE, "{}{user} used a {text:splash potion} {input#effects} onto {amount} entities{}", time, logEntry
                .getCauserUser().getName(), effects, amountAffected, loc);
         */
    }
}
