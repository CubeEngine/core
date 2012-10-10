package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * This class provides methods to register and unregister tasks and the global
 * ScheduledExecutorService is provided by this class.
 * 
 * TODO method documentation not possible as the API is not final yet
 */
public class TaskManager
{
    private final BukkitCore corePlugin;
    private final ScheduledExecutorService executorService;
    private final BukkitScheduler bukkitScheduler;

    public TaskManager(Core core, ScheduledExecutorService executorService, BukkitScheduler bukkitScheduler)
    {
        this.corePlugin = (BukkitCore)core;
        this.executorService = executorService;
        this.bukkitScheduler = bukkitScheduler;
    }

    /**
     * Returns the global ScheduledExecutorService
     *
     * @return the global ScheduledExecutorService
     */
    public ScheduledExecutorService getExecutorService()
    {
        return this.executorService;
    }

    public int scheduleSyncDelayedTask(Runnable runnable, long delay)
    {
        return this.bukkitScheduler.scheduleSyncDelayedTask(this.corePlugin, runnable, delay);
    }

    public int scheduleSyncDelayedTask(Runnable runnable)
    {
        return this.bukkitScheduler.scheduleSyncDelayedTask(this.corePlugin, runnable);
    }

    public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long interval)
    {
        return this.bukkitScheduler.scheduleSyncRepeatingTask(this.corePlugin, runnable, delay, interval);
    }

    public int scheduleAsyncDelayedTask(Runnable runnable, long delay)
    {
        return this.bukkitScheduler.scheduleAsyncDelayedTask(this.corePlugin, runnable, delay);
    }

    public int scheduleAsyncDelayedTask(Runnable runnable)
    {
        return this.bukkitScheduler.scheduleAsyncDelayedTask(this.corePlugin, runnable);
    }

    public int scheduleAsyncRepeatingTask(Runnable runnable, long delay, long interval)
    {
        return this.scheduleAsyncRepeatingTask(runnable, delay, interval);
    }

    public <T> Future<T> callSyncMethod(Callable<T> callable)
    {
        return this.bukkitScheduler.callSyncMethod(this.corePlugin, callable);
    }

    public void cancelTask(int i)
    {
        this.bukkitScheduler.cancelTask(i);
    }

    /**
     * Checks whether the given task ID revers to a task that's currently running
     *
     * @param taskID the task ID
     * @return true if there is a running task for this ID
     */
    public boolean isCurrentlyRunning(int taskID)
    {
        return this.bukkitScheduler.isCurrentlyRunning(taskID);
    }

    /**
     * Checks whether the given task ID is 
     * @param taskID the task ID
     * @return true if there is a task for this ID
     */
    public boolean isQueued(int taskID)
    {
        return this.bukkitScheduler.isQueued(taskID);
    }
}
