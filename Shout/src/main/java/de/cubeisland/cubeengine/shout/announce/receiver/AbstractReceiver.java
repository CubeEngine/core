package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import de.cubeisland.cubeengine.shout.announce.MessageOfTheDay;

import java.util.Queue;

public abstract class AbstractReceiver implements Receiver
{
    private final AnnouncementManager announcementManager;
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
            return new Pair<Announcement, Integer>(motd, (int)(motd.getDelay() / announcementManager.getGreatestCommonDivisor(this)));
        }
        for (int x = 0; x < this.announcements.size(); x++)
        {
            Announcement announcement = this.announcements.poll();

            if (this.canReceiver(announcement))
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
