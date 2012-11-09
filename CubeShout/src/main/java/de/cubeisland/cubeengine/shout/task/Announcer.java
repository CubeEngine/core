package de.cubeisland.cubeengine.shout.task;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.shout.Shout;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Class to manage tasks based on the system time, not bukkits.
 */
public class Announcer
{
    private Timer timer;
    private Map<String, TimerTask> tasks;
    private int initDelay;

    public Announcer(int initDelay)
    {
        this.timer = new Timer();
        this.tasks = new HashMap<String, TimerTask>();
        this.initDelay = initDelay;
    }

    /**
     * Schedule a task based on system time.
     * 
     * @param	task	The task to scheduler
     * @param	delay	Delay between each time this task in run.
     */
    public void scheduleTask(String user, TimerTask task, long delay)
    {
        tasks.put(user, task);
        timer.schedule(task, this.initDelay, delay);
    }

    /**
     * Stops a user from receiving announcements
     * 
     * @param user
     */
    public void stopUser(String user)
    {
        tasks.get(user).cancel();
    }
}
