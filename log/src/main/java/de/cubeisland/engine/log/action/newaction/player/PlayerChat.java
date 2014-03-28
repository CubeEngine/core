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

import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.newaction.ActionTypeBase;

import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

/**
 * Represents a player chatting
 */
public class PlayerChat extends PlayerActionType<PlayerActionListener>
{
    // return "player-chat";
    // return this.lm.getConfig(world).PLAYER_CHAT_enable;

    private String message;
    private String messageFormat;

    @Override
    public boolean canAttach(ActionTypeBase action)
    {
        return action instanceof PlayerChat
            && this.player.equals(((PlayerChat)action).player)
            && ((PlayerChat)action).message.equalsIgnoreCase(this.message)
            && Math.abs(TimeUnit.MILLISECONDS.toSeconds(action.date.getTime() - this.date.getTime())) < 30;
    }

    @Override
    public String translateAction(User user)
    {
        if (this.hasAttached())
        {
            if (this.getAttached().size() >= 4)
            {
                return user.getTranslation(POSITIVE, "{user} spammed \"{input#message}\" x{amount}",
                                           this.player.name, this.message, this.getAttached().size() + 1);
            }
            return user.getTranslation(POSITIVE, "{user} chatted \"{input#message}\" x{amount}",
                                       this.player.name, this.message, this.getAttached().size() + 1);
        }
        return user.getTranslation(POSITIVE, "{user} chatted \"{input#message}\"",
                                   this.player.name, this.message);
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public void setMessageFormat(String format)
    {
        this.messageFormat = format;
    }
}
