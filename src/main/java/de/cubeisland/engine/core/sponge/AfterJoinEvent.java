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
package de.cubeisland.engine.core.sponge;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.AbstractEvent;
import org.spongepowered.api.text.Text;

public class AfterJoinEvent extends AbstractEvent
{
    private Text joinMessage;
    private Player player;

    public AfterJoinEvent(Player player, Text joinMessage)
    {
        this.joinMessage = joinMessage;
        this.player = player;
    }

    /**
     * @return the joinMessage
     */
    public Text getJoinMessage()
    {
        return joinMessage;
    }

    /**
     * @return the player
     */
    public Player getPlayer()
    {
        return player;
    }
}
