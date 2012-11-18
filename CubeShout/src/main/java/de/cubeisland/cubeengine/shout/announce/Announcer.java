package de.cubeisland.cubeengine.shout.announce;

import de.cubeisland.cubeengine.core.user.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
     * @param	task	 The task to scheduler
     * @param	delay	Delay between each time this task in run.
     */
    public void scheduleTask(String receiver, TimerTask task, long delay)
    {
        tasks.put(receiver, task);
        timer.schedule(task, this.initDelay, delay);
    }

    /**
     * Stops a user from receiving announcements
     *
     * @param receiver
     */
    public void stopTask(String receiver)
    {
        tasks.get(receiver).cancel();
    }

    public void stopAll()
    {
        for (TimerTask task : tasks.values())
        {
            task.cancel();
        }
        tasks.clear();
    }
}
