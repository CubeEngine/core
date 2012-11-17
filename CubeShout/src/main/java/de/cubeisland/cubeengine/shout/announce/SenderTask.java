package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.shout.announce.receiver.AnnouncementReceiver;

import java.util.concurrent.Callable;

/**
 * A class to actually send a message to a user
 */
public class SenderTask implements Callable
{
    private final AnnouncementReceiver receiver;
    private final String message;

    public SenderTask(AnnouncementReceiver receiver, String message)
    {
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public Object call() throws Exception
    {
        this.receiver.sendMessage(message);
        return null;
    }
}
