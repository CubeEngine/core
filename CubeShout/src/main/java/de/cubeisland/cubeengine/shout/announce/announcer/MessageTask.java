package de.cubeisland.cubeengine.shout.announce.announcer;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.receiver.Receiver;

public class MessageTask implements Runnable
{
    private final TaskManager tm;
    private final Receiver receiver;
    private int runs;
    private int nextExecution;

    public MessageTask(TaskManager taskManager, Receiver receiver)
    {
        this.tm = taskManager;
        this.receiver = receiver;
        this.runs = 0;
        this.nextExecution = 0;
    }

    @Override
    public void run()
    {
        if (this.runs == this.nextExecution)
        {
            Pair<Announcement, Integer> pair = receiver.getNextDelayAndAnnouncement();
            if (pair != null && pair.getLeft() != null && pair.getRight() != null)
            {
                this.tm.callSyncMethod(new SenderTask(this.receiver, pair.getLeft().getMessage(receiver.getLanguage())));
                this.nextExecution = this.runs + pair.getRight();
            }
            else
            {
                this.nextExecution = this.runs + 1;
            }
        }
        ++this.runs;
    }
}
