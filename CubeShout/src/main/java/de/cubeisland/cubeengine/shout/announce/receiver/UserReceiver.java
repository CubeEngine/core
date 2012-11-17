package de.cubeisland.cubeengine.shout.announce.receiver;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;

import java.util.Queue;

public class UserReceiver extends AbstractReceiver
{
    private final User          user;
    private Queue<Announcement> announcements;

    public UserReceiver(User user, AnnouncementManager announcementManager)
    {
        super(announcementManager);
        this.user = user;
    }

    @Override
    public String getName()
    {
        return user.getName();
    }

    @Override
    public void sendMessage(String message)
    {
        user.sendMessage("shout", message);
    }

    @Override
    public String getWorld()
    {
        return user.getWorld().getName();
    }

    @Override
    public String getLanguage()
    {
        return user.getLanguage();
    }

    @Override
    public void setWorld(String world) {} // As this is a user, this is not needed
}
