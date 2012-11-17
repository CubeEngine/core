package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.shout.announce.receiver.AnnouncementReceiver;

import java.util.TimerTask;

public class MessageTask extends TimerTask
{
    private final AnnouncementManager am;
    private final TaskManager tm;
    private final AnnouncementReceiver receiver;
    private int runs;
    private int nextExcecution;

    public MessageTask(AnnouncementManager am, TaskManager taskManager, AnnouncementReceiver receiver)
    {
        this.am = am;
        this.tm = taskManager;
        this.receiver = receiver;
        this.runs = 0;
        this.nextExcecution = 0;
    }

    @Override
    public void run()
    {
        if (this.runs == this.nextExcecution)
        {
            Pair<Announcement, Integer>  pair = receiver.getNextDelayAndAnnouncement();
            if (pair != null && pair.getLeft() != null && pair.getRight() != null)
            {
                this.tm.callSyncMethod(new SenderTask(this.receiver, pair.getLeft().getMessage(receiver.getLanguage())));
                this.nextExcecution = this.runs + pair.getRight();
            }
            else
            {
                this.nextExcecution = this.runs + 1;
            }
        }
        ++this.runs;
    }
}
