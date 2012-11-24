package de.cubeisland.cubeengine.shout.announce.announcer;

import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;

import java.util.concurrent.Callable;

/**
 * A class to actually send a message to a user
 */
public class SenderTask implements Callable<Void>
{
    private final Receiver receiver;
    private final String[] message;

    public SenderTask(Receiver receiver, String[] message)
    {
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public Void call() throws Exception
    {
        for (String line : this.message)
        {
            this.receiver.sendMessage(line);
        }
        return null;
    }
}
