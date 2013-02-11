package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.module.Module;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

/**
 * This class provides methods to register and unregister tasks and the global
 * ScheduledExecutorService is provided by this class.
 */
public class TaskManager
{
    private final BukkitCore corePlugin;
    private final ScheduledExecutorService executorService;
    private final BukkitScheduler bukkitScheduler;
    private final Map<Module, TIntSet> moduleTasks;
    private final ThreadFactory threadFactory;

    public TaskManager(Core core, ThreadFactory threadFactory, int threadPoolSize, BukkitScheduler bukkitScheduler)
    {
        this.corePlugin = (BukkitCore)core;
        this.threadFactory = threadFactory;
        this.executorService = new ScheduledThreadPoolExecutor(threadPoolSize, this.threadFactory, new RejectedExecutionHandler()
        {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
            {
                CubeEngine.getLogger().log(DEBUG, "Task " + r.getClass().getName() + " got rejected!");
            }
        });
        this.bukkitScheduler = bukkitScheduler;
        this.moduleTasks = new ConcurrentHashMap<Module, TIntSet>();
    }

    private TIntSet getModuleIDs(Module module)
    {
        return this.getModuleIDs(module, true);
    }

    private TIntSet getModuleIDs(Module module, boolean create)
    {
        TIntSet IDs = this.moduleTasks.get(module);
        if (create && IDs == null)
        {
            this.moduleTasks.put(module, IDs = new TIntHashSet());
        }
        return IDs;
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

    /**
     * Returns the thread factory used by the CubeEngine to create its threads
     *
     * @return a ThreadFactory implementation
     */
    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    /**
     * Schedules a delayed task for a module with the given delay
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    public int scheduleSyncDelayedTask(Module module, Runnable runnable, long delay)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(runnable, "The runnable must not be null!");

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.scheduleSyncDelayedTask(this.corePlugin, task, delay);
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    public int scheduleSyncDelayedTask(Module module, Runnable runnable)
    {
        return this.scheduleSyncDelayedTask(module, runnable, 0);
    }

    /**
     * Schedules a repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    public int scheduleSyncRepeatingTask(Module module, Runnable runnable, long delay, long interval)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(runnable, "The runnable must not be null!");

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.scheduleSyncRepeatingTask(this.corePlugin, task, delay, interval);
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a asynchonous delayed task for a module with the given delay
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    @SuppressWarnings("deprecation")
    public int scheduleAsyncDelayedTask(Module module, Runnable runnable, long delay)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(runnable, "The runnable must not be null!");

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.scheduleAsyncDelayedTask(this.corePlugin, task, delay);
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a asynchonous delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    public int scheduleAsyncDelayedTask(Module module, Runnable runnable)
    {
        return this.scheduleAsyncDelayedTask(module, runnable, 0);
    }

    /**
     * Schedules a asynchonous repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    @SuppressWarnings("deprecation")
    public int scheduleAsyncRepeatingTask(Module module, Runnable runnable, long delay, long interval)
    {
        Validate.notNull(module, "The module must not be null!");
        Validate.notNull(runnable, "The runnable must not be null!");

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.scheduleAsyncRepeatingTask(this.corePlugin, task, delay, interval);
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a method for execution on the main server thread
     *
     * @param <T>      the return type of the method
     * @param callable the method to call
     * @return a future object
     */
    public <T> Future<T> callSyncMethod(Callable<T> callable)
    {
        return this.bukkitScheduler.callSyncMethod(this.corePlugin, callable);
    }

    /**
     * Cancels a task of a module
     *
     * @param module the module
     * @param ID     the taskID
     */
    public void cancelTask(Module module, int ID)
    {
        this.bukkitScheduler.cancelTask(ID);
        TIntSet IDs = this.getModuleIDs(module, false);
        if (IDs != null)
        {
            IDs.remove(ID);
        }
    }

    public void cancelTasks(Module module)
    {
        TIntSet taskIDs = this.moduleTasks.remove(module);
        if (taskIDs != null)
        {
            TIntIterator iter = taskIDs.iterator();
            while (iter.hasNext())
            {
                this.bukkitScheduler.cancelTask(iter.next());
            }
        }
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
     *
     * @param taskID the task ID
     * @return true if there is a task for this ID
     */
    public boolean isQueued(int taskID)
    {
        return this.bukkitScheduler.isQueued(taskID);
    }

    private class Task implements Runnable
    {
        protected int taskID;
        private final Runnable task;
        private final TIntSet taskIDs;

        public Task(Runnable task, TIntSet taskIDs)
        {
            this.task = task;
            this.taskIDs = taskIDs;
        }

        @Override
        public void run()
        {
            this.task.run();
            this.taskIDs.remove(this.taskID);
        }
    }
}
