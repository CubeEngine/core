package de.cubeisland.cubeengine.shout.announce.announcer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;

/**
 * Class to manage futures based on the system time.
 */
public class Announcer
{
    private ScheduledExecutorService executor;
    private Map<String, ScheduledFuture> futures;
    private int initDelay;

    public Announcer(TaskManager taskManager, int initDelay)
    {
        this.executor = Executors.newSingleThreadScheduledExecutor(taskManager.getThreadFactory());
        this.futures = new HashMap<String, ScheduledFuture>();
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
        this.futures.put(receiver, this.executor.scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param receiver the receiver the task should be stopped for
     */
    public void cancel(String receiver)
    {
        ScheduledFuture future = this.futures.remove(receiver);
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * Stop all announcements to all receivers
     */
    public void shutdown()
    {
        this.futures.clear();
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
            this.executor.shutdownNow();
        }
        catch (InterruptedException ignore)
        {}
        this.executor = null;
        this.futures = null;
    }
}
