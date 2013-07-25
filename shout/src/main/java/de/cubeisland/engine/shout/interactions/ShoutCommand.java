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
package de.cubeisland.engine.shout.interactions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.shout.Shout;
import de.cubeisland.engine.shout.announce.Announcement;

public class ShoutCommand
{
    private Shout module;

    public ShoutCommand(Shout module)
    {
        this.module = module;
    }

    @Command(names = {
        "shout", "announce"
    }, min = 1, max = 1, desc = "Announce a message to players on the server", usage = "<announcement name>")
    public void shout(CommandContext context)
    {
        Announcement announcement = this.module.getAnnouncementManager().getAnnouncement(context.getString(0));
        if (announcement == null)
        {
            context.sendTranslated("&c%s was not found!", context.getString(0));
            return;
        }
        List<Player> players;

        if (announcement.getFirstWorld().equals("*"))
        {
            players = Arrays.asList(Bukkit.getOnlinePlayers());
        }
        else
        {
            players = new ArrayList<Player>();
            for (String world : announcement.getWorlds())
            {
                players.addAll(Bukkit.getWorld(world).getPlayers());
            }
        }

        for (Player player : players)
        {
            User u = this.module.getCore().getUserManager().getExactUser(player.getName());
            String[] message = announcement.getMessage(u.getLocale());
            if (message != null)
            {
                for (String line : message)
                {
                    u.sendMessage(ChatFormat.parseFormats(line));
                }
            }
        }
        context.sendTranslated("&aThe announcement &e%s&a has been announced!", announcement.getName());
    }
}
