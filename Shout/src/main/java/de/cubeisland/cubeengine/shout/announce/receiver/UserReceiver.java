package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

import java.util.Locale;
import java.util.Queue;

public class UserReceiver extends AbstractReceiver
{
    private final User user;
    private Queue<Announcement> announcements;

    public UserReceiver(User user, AnnouncementManager announcementManager)
    {
        super(announcementManager);
        this.user = user;
    }

    @Override
    public String getName()
    {
        return this.user.getName();
    }

    @Override
    public void sendMessage(String[] message)
    {
        this.user.sendMessage(" ");
        for (String line : message)
        {
            this.user.sendMessage(line);
        }
        this.user.sendMessage(" ");
    }

    @Override
    public Locale getLocale()
    {
        return this.user.getLocale();
    }

    @Override
    public boolean canReceiver(Announcement announcement)
    {
        return announcement.hasWorld(this.user.getWorld().getName());
    }

    @Override
    public boolean couldReceive(Announcement announcement)
    {
        return announcement.getPermNode().equals("*") || this.user.hasPermission(announcement.getPermNode());
    }
}
