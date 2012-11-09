package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.user.User;
import java.util.TimerTask;

public class MessageTask extends TimerTask
{
    private final AnnouncementManager aManager;
    private final TaskManager taskManager;
    private final User user;
    private int runs;
    private int nextExcecution;

    public MessageTask(AnnouncementManager aManager, TaskManager scheduler, User user)
    {
        this.aManager = aManager;
        this.taskManager = scheduler;
        this.user = user;
        this.runs = 0;
        this.nextExcecution = 0;
    }

    public void run()
    {
        if (this.runs == this.nextExcecution)
        {
            if (aManager.getNextMessage(user.getName()) != null)
            {
                taskManager.callSyncMethod(new AnnouncementSender(user, aManager.getNextMessage(user.getName())));
                this.nextExcecution = this.runs + aManager.getNextDelay(user.getName());
            }
            else
            {
                this.nextExcecution = this.runs + 1;
            }
        }
        this.runs++;
    }
}