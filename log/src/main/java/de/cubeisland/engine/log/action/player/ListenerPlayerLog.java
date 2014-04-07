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

import org.bukkit.entity.Player;

import de.cubeisland.engine.log.Log;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.LogListener;

public abstract class ListenerPlayerLog extends LogListener
{
    protected ListenerPlayerLog(Log module, Class<? extends BaseAction>... actions)
    {
        super(module, actions);
    }

    protected void setPlayerAndLocation(Player player, ActionPlayer action)
    {
        action.setLocation(player.getLocation());
        action.setPlayer(player);
    }
}
