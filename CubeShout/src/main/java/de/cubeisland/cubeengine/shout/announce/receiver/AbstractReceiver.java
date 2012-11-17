package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

import java.util.Queue;

public abstract class AbstractReceiver implements AnnouncementReceiver
{
    private final AnnouncementManager announcementManager;
    private Queue<Announcement> announcements;

    protected AbstractReceiver(AnnouncementManager announcementManager)
    {
        this.announcementManager = announcementManager;
    }

    public void setAllAnnouncements(Queue<Announcement> announcements)
    {
        announcements = announcements;
    }

    public Pair<Announcement, Integer> getNextDelayAndAnnouncement()
    {
        Pair<Announcement, Integer> pair = new Pair<Announcement, Integer>(null, null);
        pair.setLeft(announcements.poll());
        pair.setRight((int)(pair.getLeft().getDelay()/announcementManager.getGreatestCommonDivisor(this)));
        announcements.add(pair.getLeft());
        return pair;
    }

    public Queue<Announcement> getAllAnnouncements()
    {
        return announcements;
    }
}
