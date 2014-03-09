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
package de.cubeisland.engine.basics.command.general;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import de.cubeisland.engine.basics.Basics;
import de.cubeisland.engine.basics.storage.BasicsUserEntity;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class MuteListener implements Listener
{
    private final Basics basics;
    private final IgnoreCommands ignore;

    public MuteListener(Basics basics, IgnoreCommands ignore)
    {
        this.basics = basics;
        this.ignore = ignore;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        if (!event.getMessage().startsWith("/"))
        {
            // muted?
            User sender = this.basics.getCore().getUserManager().getExactUser(event.getPlayer().getName());
            if (sender != null)
            {
                BasicsUserEntity bUser = basics.getBasicsUser(sender).getbUEntity();
                if (bUser.getMuted() != null && System.currentTimeMillis() < bUser.getMuted().getTime())
                {
                    event.setCancelled(true);
                    sender.sendTranslated(MessageType.NEGATIVE, "You try to speak but nothing happens!");
                }
            }
            // ignored?
            ArrayList<Player> ignore = new ArrayList<>();
            for (Player player : event.getRecipients())
            {
                User user = this.basics.getCore().getUserManager().getExactUser(player.getName());
                if (this.ignore.checkIgnored(user, sender))
                {
                    ignore.add(player);
                }
            }
            event.getRecipients().removeAll(ignore);
        }
    }
}
