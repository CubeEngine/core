package de.cubeisland.cubeengine.core.util.worker;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This TaskQueue will execute one task every serverTick.
 */
public class SyncTaskQueue implements TaskQueue
{
    private final Worker          workerTask = new Worker();
    private final BukkitCore      corePlugin;
    private final BukkitScheduler scheduler;
    private final Queue<Runnable> taskQueue;
    private int                   taskID;
    private boolean               isShutdown;

    public SyncTaskQueue(Core core)
    {
        this(core, new LinkedList<Runnable>());
    }

    public SyncTaskQueue(Core core, Queue<Runnable> taskQueue)
    {
        this.corePlugin = (BukkitCore)core;
        this.scheduler = core.getServer().getScheduler();
        this.taskQueue = taskQueue;
        this.taskID = -1;
        this.isShutdown = false;
    }

    public synchronized void run()
    {
        if (this.taskQueue.isEmpty())
        {
            this.scheduler.cancelTask(this.taskID);
            return;
        }
        this.taskQueue.poll().run();
    }

    @Override
    public synchronized void addTask(Runnable runnable)
    {
        if (!this.isShutdown)
        {
            return;
        }
        Validate.notNull(runnable, "The runnable must not be null!");

        this.taskQueue.offer(runnable);
        this.start();
    }

    @Override
    public synchronized void start()
    {
        if (!this.isRunning())
        {
            this.taskID = this.scheduler.scheduleSyncRepeatingTask(this.corePlugin, this.workerTask, 0, 1);
        }
    }

    @Override
    public void shutdown()
    {
        this.isShutdown = true;
        this.taskQueue.clear();
        this.stop();
    }

    @Override
    public boolean isShutdown()
    {
        return this.isShutdown;
    }

    @Override
    public synchronized void stop()
    {
        this.stop(false);
    }

    @Override
    public synchronized void stop(boolean interupt)
    {
        if (this.isRunning())
        {
            this.scheduler.cancelTask(this.taskID);
            this.taskID = -1;
        }
    }

    @Override
    public synchronized boolean isRunning()
    {
        return this.taskID > -1;
    }

    @Override
    public synchronized int size()
    {
        return this.taskQueue.size();
    }

    private class Worker implements Runnable
    {
        @Override
        public void run()
        {
            taskQueue.poll().run();
            if (taskQueue.isEmpty())
            {
                stop();
            }
        }
    }
}
