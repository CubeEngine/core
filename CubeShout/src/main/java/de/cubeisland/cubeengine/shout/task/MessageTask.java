package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageTask extends TimerTask
{
    AnnouncementManager aManager;
    Announcer taskManager;
    String user;
    int runs;
    int nextExcecution;

    public MessageTask(AnnouncementManager aManager, Announcer scheduler, User user)
    {
        this.aManager = aManager;
        this.taskManager = scheduler;
        this.user = user.getName();
        this.runs = 0;
        this.nextExcecution = 0;
    }

    public void run()
    {
        if (this.runs == this.nextExcecution)
        {
            if (aManager.getNextMessage(user) != null)
            {
                taskManager.queueMessage(user, aManager.getNextMessage(user));
                this.nextExcecution = this.runs + aManager.getNextDelay(user);
            }
            else
            {
                this.nextExcecution = this.runs + 1;
            }
        }
        runs++;
    }
}