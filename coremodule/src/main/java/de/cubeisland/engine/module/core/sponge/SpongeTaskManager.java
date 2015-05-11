/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p/>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.module.core.sponge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.task.TaskManager;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.service.scheduler.Task;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

@ServiceImpl(TaskManager.class)
@Version(1)
public class SpongeTaskManager implements TaskManager
{
    private final CoreModule corePlugin;
    private AsynchronousScheduler asyncScheduler;
    private SynchronousScheduler syncScheduler;
    private final Map<Module, Set<UUID>> moduleTasks;

    @Inject
    public SpongeTaskManager(CoreModule core, AsynchronousScheduler asyncScheduler, SynchronousScheduler syncScheduler)
    {
        this.corePlugin = core;
        this.asyncScheduler = asyncScheduler;
        this.syncScheduler = syncScheduler;
        this.moduleTasks = new ConcurrentHashMap<>();
    }

    private Set<UUID> getModuleIDs(Module module)
    {
        return this.getModuleIDs(module, true);
    }

    private Set<UUID> getModuleIDs(Module module, boolean create)
    {
        Set<UUID> IDs = this.moduleTasks.get(module);
        if (create && IDs == null)
        {
            this.moduleTasks.put(module, IDs = new HashSet<>());
        }
        return IDs;
    }


    @Override
    public Optional<UUID> runTask(Module module, Runnable runnable)
    {
        return this.runTaskDelayed(module, runnable, 0);
    }

    @Override
    public Optional<UUID> runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, syncScheduler.runTaskAfter(corePlugin, runnable, delay));
    }

    @Override
    public Optional<UUID> runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, syncScheduler.runRepeatingTaskAfter(corePlugin, runnable, delay, interval));
    }

    @Override
    public Optional<UUID> runAsynchronousTask(Module module, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(module, runnable, 0);
    }

    @Override
    public Optional<UUID> runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");
        return addTaskId(module, asyncScheduler.runTaskAfter(corePlugin, runnable, TimeUnit.MILLISECONDS, delay * 50));
    }

    private Optional<UUID> addTaskId(Module module, Optional<Task> task)
    {
        final Set<UUID> tasks = this.getModuleIDs(module);
        if (task.isPresent())
        {
            tasks.add(task.get().getUniqueId());
            return Optional.of(task.get().getUniqueId());
        }
        return Optional.absent();
    }

    @Override
    public Optional<UUID> runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, asyncScheduler.runRepeatingTaskAfter(corePlugin, runnable, TimeUnit.MILLISECONDS,
                                                                      delay * 50, interval * 50));
    }

    @Override
    public <T> Future<T> callSync(Callable<T> callable)
    {
        expectNotNull(callable, "The callable must not be null!");
        return this.syncScheduler.runTask(corePlugin, callable);
    }

    @Override
    public void cancelTask(Module module, UUID uuid)
    {
        Optional<Task> task = syncScheduler.getTaskById(uuid);
        if (task.isPresent())
        {
            task.get().cancel();
        }
        task = asyncScheduler.getTaskById(uuid);
        if (task.isPresent())
        {
            task.get().cancel();
        }

        Set<UUID> moduleIDs = getModuleIDs(module);
        if (moduleIDs != null)
        {
            moduleIDs.remove(uuid);
        }
    }

    @Override
    public void cancelTasks(Module module)
    {
        Set<UUID> taskIDs = this.moduleTasks.remove(module);
        if (taskIDs != null)
        {
            for (UUID taskID : taskIDs)
            {
                cancelTask(module, taskID);
            }
        }
    }

    @Override
    public boolean isCurrentlyRunning(UUID taskID)
    {
        this.syncScheduler.getTaskById(taskID); // TODO
    }

    @Override
    public boolean isQueued(UUID taskID)
    {
        this.syncScheduler.getTaskById(taskID); // TODO
    }

    @Override
    public synchronized void clean(Module module)
    {
        this.cancelTasks(module);
    }

    private class CETask implements Runnable
    {
        protected int taskID;
        private final Runnable task;
        private final Set<Integer> taskIDs;

        public CETask(Runnable task, Set<Integer> taskIDs)
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
