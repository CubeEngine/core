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
package de.cubeisland.engine.log.action.player;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.LoggingConfiguration;
import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;
import static de.cubeisland.engine.log.action.ActionCategory.PLAYER;

/**
 * Represents a Player joining the server
 */
public class PlayerJoin extends ActionPlayer
{
    public String ip;

    public PlayerJoin()
    {
        super("join", PLAYER);
    }

    @Override
    public boolean canAttach(BaseAction action)
    {
        return action instanceof PlayerJoin && this.player.equals(((PlayerJoin)action).player);
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            return user.getTranslation(POSITIVE, "{user} joined the server x{amount}", this.player.name,
                                       this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} joined the server", this.player.name);
        // TODO ip if set
    }

    public void setIp(String ip)
    {
        this.ip = ip;
    }

    @Override
    public boolean isActive(LoggingConfiguration config)
    {
        return config.player.join_enable;
    }
}
