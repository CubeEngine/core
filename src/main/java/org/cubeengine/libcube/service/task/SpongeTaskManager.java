/*
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
package org.cubeengine.libcube.service.task;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.cubeengine.libcube.LibCube;
import org.cubeengine.libcube.ModuleManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SpongeTaskManager implements TaskManager
{
    private final Map<Class, Set<UUID>> tasks;
    private ModuleManager mm;
    private PluginContainer plugin;

    @Inject
    public SpongeTaskManager(ModuleManager mm)
    {
        this.mm = mm;
        this.plugin = mm.getPlugin(LibCube.class).get();
        this.tasks = new ConcurrentHashMap<>();
        mm.registerBinding(TaskManager.class, this);
    }

    private Set<UUID> getTaskIDs(Class owner)
    {
        return this.getTaskIDs(owner, true);
    }

    private Set<UUID> getTaskIDs(Class owner, boolean create)
    {
        Set<UUID> IDs = this.tasks.get(owner);
        if (create && IDs == null)
        {
            this.tasks.put(owner, IDs = new HashSet<>());
        }
        return IDs;
    }

    @Override
    public UUID runTask(Class owner, Runnable runnable)
    {
        return this.runTaskDelayed(owner, runnable, 0);
    }

    @Override
    public UUID runTaskDelayed(Class owner, Runnable runnable, long delay)
    {
        checkNotNull(owner, "The module must not be null!");
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = Task.builder().delayTicks(delay).execute(runnable).plugin(getPlugin(owner)).build();
        return addTaskId(owner, Sponge.getServer().getScheduler().submit(task));
    }

    @Override
    public UUID runTimer(Class owner, Runnable runnable, long delay, long interval)
    {
        checkNotNull(owner, "The module must not be null!");
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = Task.builder().delayTicks(delay).intervalTicks(interval).execute(runnable).plugin(getPlugin(owner)).build();
        return addTaskId(owner, Sponge.getServer().getScheduler().submit(task));
    }

    private PluginContainer getPlugin(Class owner)
    {
        return this.mm.getPlugin(owner).orElse(this.plugin);
    }

    @Override
    public UUID runAsynchronousTask(Class owner, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(owner, runnable, 0);
    }

    @Override
    public UUID runAsynchronousTaskDelayed(Class owner, Runnable runnable, long delay)
    {
        checkNotNull(owner, "The module must not be null!");
        checkNotNull(runnable, "The runnable must not be null!");
        final Task task = Task.builder().delay(delay * 50, MILLISECONDS).execute(runnable).plugin(getPlugin(owner)).build();
        return addTaskId(owner, Sponge.getAsyncScheduler().submit(task));
    }

    private UUID addTaskId(Class owner, ScheduledTask task)
    {
        final Set<UUID> tasks = this.getTaskIDs(owner);
        tasks.add(task.getUniqueId());
        return task.getUniqueId();
    }

    @Override
    public UUID runAsynchronousTimer(Class owner, Runnable runnable, long delay, long interval)
    {
        checkNotNull(owner, "The module must not be null!");
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = Task.builder().delay(delay * 50, MILLISECONDS).execute(runnable).interval(interval * 50, MILLISECONDS).plugin(getPlugin(owner)).build();
        return addTaskId(owner, Sponge.getAsyncScheduler().submit(task));
    }

    @Override
    public void cancelTask(Class owner, UUID uuid)
    {
        Optional<ScheduledTask> task = Sponge.getServer().getScheduler().getTaskById(uuid);
        task.ifPresent(ScheduledTask::cancel);

        Set<UUID> ownerIds = getTaskIDs(owner);
        if (ownerIds != null)
        {
            ownerIds.remove(uuid);
        }
    }

    @Override
    public void cancelTasks(Class owner)
    {
        Set<UUID> taskIDs = this.tasks.remove(owner);
        if (taskIDs != null)
        {
            for (UUID taskID : taskIDs)
            {
                cancelTask(owner, taskID);
            }
        }
    }

    @Override
    public synchronized void clean(Class owner)
    {
        this.cancelTasks(owner);
    }

}
