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
package de.cubeisland.engine.log.action.player.item;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.player.ActionPlayer;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.USE;

/**
 * Represents a player using SplashPotions
 */
public class UsePotionSplash extends ActionPlayer // TODO potion item
{
    public UsePotionSplash()
    {
        super("splashpotion", USE);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return false; // TODO
    }

    @Override
    public String translateAction(User user)
    {
        return user.getTranslation(POSITIVE, "{user} splashed a potion", this.player.name);
        /*
        user.sendTranslated(MessageType.POSITIVE, "{}{user} used {amount} splash potions {input#effects} onto {amount} entities in total{}", time, logEntry
                .getCauserUser().getName(), logEntry.getAttached().size() + 1, effects, amountAffected, loc);
        user.sendTranslated(MessageType.POSITIVE, "{}{user} used a {text:splash potion} {input#effects} onto {amount} entities{}", time, logEntry
                .getCauserUser().getName(), effects, amountAffected, loc);
         */
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.use.splashpotion;
    }
}
