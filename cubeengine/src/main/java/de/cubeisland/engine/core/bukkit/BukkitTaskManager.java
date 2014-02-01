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

import org.bukkit.scheduler.BukkitScheduler;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleThreadFactory;
import de.cubeisland.engine.core.task.TaskManager;
import de.cubeisland.engine.core.task.worker.CoreThreadFactory;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

public class BukkitTaskManager implements TaskManager
{
    private final BukkitCore corePlugin;
    private final BukkitScheduler bukkitScheduler;
    private final Map<Module, TIntSet> moduleTasks;
    private final CoreThreadFactory threadFactory;
    private final Map<String, ModuleThreadFactory> moduleThreadFactories;

    public BukkitTaskManager(Core core, BukkitScheduler bukkitScheduler)
    {
        this.corePlugin = (BukkitCore)core;
        this.threadFactory = new CoreThreadFactory(core);
        this.bukkitScheduler = bukkitScheduler;
        this.moduleTasks = new ConcurrentHashMap<>();
        this.moduleThreadFactories = new THashMap<>();
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

    public CoreThreadFactory getThreadFactory()
    {
        return this.threadFactory;
    }

    public synchronized ModuleThreadFactory getThreadFactory(Module module)
    {
        ModuleThreadFactory threadFactory = this.moduleThreadFactories.get(module.getId());
        if (threadFactory == null)
        {
            this.moduleThreadFactories.put(module.getId(), threadFactory = new ModuleThreadFactory(module));
        }
        return threadFactory;
    }

    public int runTask(Module module, Runnable runnable)
    {
        return this.runTaskDelayed(module, runnable, 0);
    }

    public int runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

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

    public int runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

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

    public int runAsynchronousTask(Module module, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(module, runnable, 0);
    }

    public int runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

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

    public int runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

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

    public <T> Future<T> callSync(Callable<T> callable)
    {
        expectNotNull(callable, "The callable must not be null!");
        return this.bukkitScheduler.callSyncMethod(this.corePlugin, callable);
    }

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
            TIntIterator it = taskIDs.iterator();
            while (it.hasNext())
            {
                this.bukkitScheduler.cancelTask(it.next());
            }
        }
    }

    public boolean isCurrentlyRunning(int taskID)
    {
        return this.bukkitScheduler.isCurrentlyRunning(taskID);
    }

    public boolean isQueued(int taskID)
    {
        return this.bukkitScheduler.isQueued(taskID);
    }

    public synchronized void clean(Module module)
    {
        this.cancelTasks(module);
        final ModuleThreadFactory factory = this.moduleThreadFactories.remove(module.getId());
        if (factory != null)
        {
            factory.shutdown();
        }
    }

    @Override
    public synchronized void clean()
    {
        for (ModuleThreadFactory factory : this.moduleThreadFactories.values())
        {
            factory.shutdown();
        }
        this.moduleThreadFactories.clear();
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
