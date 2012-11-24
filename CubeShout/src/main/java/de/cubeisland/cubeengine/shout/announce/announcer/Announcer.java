package de.cubeisland.cubeengine.shout.announce.announcer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class to manage tasks based on the system time, not bukkits.
 */
public class Announcer
{
    private ScheduledExecutorService     executor;
    private Map<String, ScheduledFuture> tasks;
    private int                          initDelay;

    public Announcer(int initDelay)
    {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.tasks = new HashMap<String, ScheduledFuture>();
        this.initDelay = initDelay;
    }

    /**
     * Schedule a task based on the system time.
     *
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleTask(String receiver, Runnable task, long delay)
    {
        this.tasks.put(receiver, this.executor.scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param receiver
     */
    public void stopTask(String receiver)
    {
        this.tasks.get(receiver).cancel(false);
    }

    /**
     * Stop all announcements to all receivers
     */
    public void stopAll()
    {
        this.tasks.clear();
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignore)
        {}
    }
}
