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
package de.cubeisland.engine.shout.announce.receiver;

import java.util.Queue;

import de.cubeisland.engine.core.util.Pair;
import de.cubeisland.engine.shout.announce.Announcement;
import de.cubeisland.engine.shout.announce.AnnouncementManager;
import de.cubeisland.engine.shout.announce.MessageOfTheDay;

public abstract class AbstractReceiver implements Receiver
{
    protected final AnnouncementManager announcementManager;
    private boolean motdShown = false;
    private MessageOfTheDay motd;
    private Queue<Announcement> announcements;

    protected AbstractReceiver(AnnouncementManager announcementManager)
    {
        this.announcementManager = announcementManager;
    }

    public void setAllAnnouncements(Queue<Announcement> announcements)
    {
        this.announcements = announcements;
    }

    public Pair<Announcement, Integer> getNextDelayAndAnnouncement()
    {
        if (!motdShown && motd != null)
        {
            this.motdShown = true;
            return new Pair<Announcement, Integer>(motd, (int)(motd.getDelay() / announcementManager.getGreatestCommonDivisor(this)));
        }
        for (int x = 0; x < this.announcements.size(); x++)
        {
            Announcement announcement = this.announcements.poll();
            this.announcements.add(announcement);
            if (this.canReceive(announcement))
            {
                return new Pair<Announcement, Integer>(announcement, (int)(announcement.getDelay() / announcementManager.getGreatestCommonDivisor(this)));
            }
        }
        return null;
    }

    public Queue<Announcement> getAllAnnouncements()
    {
        return announcements;
    }

    public void setMOTD(MessageOfTheDay motd)
    {
        this.motd = motd;
    }
}
