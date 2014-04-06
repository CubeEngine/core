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
package de.cubeisland.engine.shout.announce.announcer;

import java.util.Locale;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.shout.announce.Announcement;
import de.cubeisland.engine.shout.announce.MessageOfTheDay;
import de.cubeisland.engine.shout.announce.receiver.Receiver;

public class FixedCycleTask implements Runnable
{
    private final UserManager userManager;
    private final TaskManager taskManager;
    private final Announcement announcement;

    public FixedCycleTask(UserManager userManager, TaskManager taskManager, Announcement announcement)
    {
        this.userManager = userManager;
        this.taskManager = taskManager;
        this.announcement = announcement;
    }


    @Override
    public void run()
    {
        if (announcement.getFirstWorld().equals("*"))
        {
            for (User user : userManager.getOnlineUsers())
            {
                taskManager.callSync(new SenderTask(announcement.getMessage(user.getLocale()), new SimpleReceiver(user)));
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
                        if (this.announcement.canAccess(player))
                        {
                            User user = userManager.getExactUser(player.getUniqueId());
                            taskManager.callSync(new SenderTask(announcement.getMessage(user.getLocale()), new SimpleReceiver(user)));
                        }
                    }
                }
            }
        }
    }

    private final class SimpleReceiver implements Receiver
    {
        private final User user;

        protected SimpleReceiver(User user)
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
        public void addAnnouncement(Announcement announcement)
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
