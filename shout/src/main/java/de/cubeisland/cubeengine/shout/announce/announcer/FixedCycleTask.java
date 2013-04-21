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
package de.cubeisland.cubeengine.shout.announce.announcer;

import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.Shout;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.MessageOfTheDay;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;

public class FixedCycleTask implements Runnable
{
    private final Shout module;
    private final Announcement announcement;

    public FixedCycleTask(Shout module, Announcement announcement)
    {
        this.module = module;
        this.announcement = announcement;
    }


    @Override
    public void run()
    {
        if (announcement.getFirstWorld().equals("*"))
        {
            for (User user : module.getCore().getUserManager().getOnlineUsers())
            {
                module.getCore().getTaskManager().callSyncMethod(
                    new SenderTask(announcement.getMessage(user.getLocale()), new CleanReceiver(user)));
            }
        }
        else
        {
            for (String w : announcement.getWorlds())
            {
                World world = Bukkit.getWorld(w);
                if (world != null)
                {
                    for (Player player : world.getPlayers())
                    {
                        User user = module.getCore().getUserManager().getUser(player);
                        module.getCore().getTaskManager().callSyncMethod(
                            new SenderTask(announcement.getMessage(user.getLocale()), new CleanReceiver(user)));
                    }
                }
            }
        }
    }

    private final class CleanReceiver implements Receiver
    {
        private final User user;

        protected CleanReceiver(User user)
        {
            this.user = user;
        }

        @Override
        public void sendMessage(String[] message)
        {
            for (String line : message)
            {
                user.sendMessage(ChatFormat.parseFormats(line));
            }
        }

        @Override
        public String getName()
        {return null;}

        @Override
        public Pair<Announcement, Integer> getNextDelayAndAnnouncement()
        {return null;}

        @Override
        public Queue<Announcement> getAllAnnouncements()
        {return null;}

        @Override
        public Locale getLocale()
        {return null;}

        @Override
        public void setAllAnnouncements(Queue<Announcement> announcements)
        {}

        @Override
        public boolean canReceive(Announcement announcement)
        {return false;}

        @Override
        public boolean couldReceive(Announcement announcement)
        {return false;}

        @Override
        public void setMOTD(MessageOfTheDay motd)
        {}
    }
}
