/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.bukkit;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.bukkit.scheduler.BukkitScheduler;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.task.TaskManager;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class BukkitTaskManager implements TaskManager
{
    private final BukkitCore corePlugin;
    private final BukkitScheduler bukkitScheduler;
    private final Map<Module, TIntSet> moduleTasks;
    private final ThreadFactory threadFactory;

    public BukkitTaskManager(Core core, ThreadFactory threadFactory, BukkitScheduler bukkitScheduler)
    {
        this.corePlugin = (BukkitCore)core;
        this.threadFactory = threadFactory;
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
     * Returns the thread factory used by the CubeEngine to create its threads
     *
     * @return a ThreadFactory implementation
     */
    public ThreadFactory getThreadFactory()
    {
        return this.threadFactory;
    }

    /**
     * Schedules a delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    public int runTask(Module module, Runnable runnable)
    {
        return this.runTaskDelayed(module, runnable, 0);
    }

    /**
     * Schedules a delayed task for a module with the given delay on the main server thread
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    public int runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        assert module != null: "The module must not be null!";
        assert runnable != null: "The runnable must not be null!";

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
     * Schedules a repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    public int runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        assert module != null: "The module must not be null!";
        assert runnable != null: "The runnable must not be null!";

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskTimer(this.corePlugin, task, delay, interval).getTaskId();
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a asynchronous delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    public int runAsynchronousTask(Module module, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(module, runnable, 0);
    }

    /**
     * Schedules a asynchronous delayed task for a module with the given delay
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    public int runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        assert module != null: "The module must not be null!";
        assert runnable != null: "The runnable must not be null!";

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskLaterAsynchronously(this.corePlugin, task, delay).getTaskId();
        if (taskID > -1)
        {
            task.taskID = taskID;
            tasks.add(taskID);
        }
        return taskID;
    }

    /**
     * Schedules a asynchronous repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    public int runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        assert module != null: "The module must not be null!";
        assert runnable != null: "The runnable must not be null!";

        final TIntSet tasks = this.getModuleIDs(module);
        final Task task = new Task(runnable, tasks);
        final int taskID = this.bukkitScheduler.runTaskTimerAsynchronously(this.corePlugin, task, delay, interval).getTaskId();
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
    public <T> Future<T> callSync(Callable<T> callable)
    {
        assert callable != null: "The callable must not be null!";
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

    @Override
    public void clean()
    {

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
