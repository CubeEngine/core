package de.cubeisland.cubeengine.shout.announce.announcer;

import de.cubeisland.cubeengine.shout.announce.receiver.AnnouncementReceiver;

import java.util.concurrent.Callable;

/**
 * A class to actually send a message to a user
 */
public class SenderTask implements Callable<Void>
{
    private final AnnouncementReceiver receiver;
    private final String message;

    public SenderTask(AnnouncementReceiver receiver, String message)
    {
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public Void call() throws Exception
    {
        this.receiver.sendMessage(this.message);
        return null;
    }
}
