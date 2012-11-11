package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;

public class MessageTask extends TimerTask
{
    private final AnnouncementManager am;
    private final TaskManager tm;
    private final User user;
    private int runs;
    private int nextExcecution;

    public MessageTask(AnnouncementManager am, TaskManager taskManager, User user)
    {
        this.am = am;
        this.tm = taskManager;
        this.user = user;
        this.runs = 0;
        this.nextExcecution = 0;
    }

    public void run()
    {
        if (this.runs == this.nextExcecution)
        {
            if (am.getNextMessage(user.getName()) != null)
            {
                this.tm.callSyncMethod(new AnnouncementSender(this.user, this.am.getNextMessage(this.user.getName())));
                this.nextExcecution = this.runs + this.am.getNextDelay(user.getName());
            }
            else
            {
                this.nextExcecution = this.runs + 1;
            }
        }
        ++this.runs;
    }
}