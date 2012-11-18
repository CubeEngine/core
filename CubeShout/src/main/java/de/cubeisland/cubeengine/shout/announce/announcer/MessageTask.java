package de.cubeisland.cubeengine.shout.announce.announcer;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.Announcement;
import de.cubeisland.cubeengine.shout.announce.AnnouncementManager;
import de.cubeisland.cubeengine.shout.announce.receiver.AnnouncementReceiver;

import java.util.TimerTask;

public class MessageTask extends TimerTask
{
    private final AnnouncementManager am;
    private final TaskManager tm;
    private final AnnouncementReceiver receiver;
    private int runs;
    private int nextExecution;

    public MessageTask(AnnouncementManager am, TaskManager taskManager, AnnouncementReceiver receiver)
    {
        this.am = am;
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
