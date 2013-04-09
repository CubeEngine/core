package de.cubeisland.cubeengine.shout.announce.announcer;

import java.util.concurrent.Callable;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;

class SenderTask implements Callable<Void>
{
    private final String[] message;
    private final Receiver receiver;

    public SenderTask(String[] message, Receiver receiver)
    {
        this.message = message;
        this.receiver = receiver;
    }

    @Override
    public Void call() throws Exception
    {
        String[] newline = {""};
        receiver.sendMessage(newline);
        receiver.sendMessage(this.message);
        receiver.sendMessage(newline);
        return null;
    }
}