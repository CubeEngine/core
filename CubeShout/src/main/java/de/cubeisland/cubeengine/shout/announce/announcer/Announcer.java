package de.cubeisland.cubeengine.shout.announce.announcer;

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
     * Schedule a task based on the system time.
     *
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleTask(String receiver, TimerTask task, long delay)
    {
        tasks.put(receiver, task);
        timer.schedule(task, this.initDelay, delay);
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param receiver
     */
    public void stopTask(String receiver)
    {
        tasks.get(receiver).cancel();
    }

    /**
     * Stop all announcements to all receivers
     */
    public void stopAll()
    {
        for (TimerTask task : tasks.values())
        {
            task.cancel();
        }
        tasks.clear();
    }
}
