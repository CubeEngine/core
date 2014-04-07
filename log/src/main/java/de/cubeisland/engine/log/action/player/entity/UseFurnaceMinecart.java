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
package de.cubeisland.engine.log.action.player.entity;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.ENTITY;
import static de.cubeisland.engine.log.action.ActionCategory.USE;

/**
 * Represents a player fueling a furnace-minecart
 */
public class UseFurnaceMinecart extends ActionPlayerEntity
{
    public UseFurnaceMinecart()
    {
        super("furnacecart", USE);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof UseFurnaceMinecart && this.player.equals(((UseFurnaceMinecart)action).player)
            && this.entity.equals(((UseFurnaceMinecart)action).entity);
    }

    @Override
    public String translateAction(User user)
    {
        int count = this.countAttached();
        return user.getTranslationN(POSITIVE, count, "{user} gave fuel to a furnace-minecart",
                                    "{user} gave fuel to a furnace-minecart {amount} times", this.player.name, count);
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.use.furnaceMinecart;
    }
}
