package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.user.User;
import java.util.concurrent.Callable;

/**
 * A class to actually send a message to a user
 */
public class AnnouncementSender implements Callable
{
    private final User   user;
    private final String message;

    public AnnouncementSender(User user, String message)
    {
        this.user = user;
        this.message = message;
    }

    @Override
    public Object call() throws Exception
    {
        this.user.sendMessage(message);
        return null;
    }
}
