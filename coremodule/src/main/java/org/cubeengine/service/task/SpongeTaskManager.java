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
package org.cubeengine.service.task;

import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;
import org.spongepowered.api.Game;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.cubeengine.module.core.contract.Contract.expectNotNull;

@ServiceImpl(TaskManager.class)
@Version(1)
public class SpongeTaskManager implements TaskManager
{
    private final Object plugin;
    private Scheduler scheduler;
    private final Map<Module, Set<UUID>> moduleTasks;

    @Inject
    public SpongeTaskManager(Game game)
    {
        this.plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance();
        this.scheduler = game.getScheduler();
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
    public UUID runTask(Module module, Runnable runnable)
    {
        return this.runTaskDelayed(module, runnable, 0);
    }

    @Override
    public UUID runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, scheduler.createTaskBuilder().delayTicks(delay).execute(runnable).submit(plugin));
    }

    @Override
    public UUID runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, scheduler.createTaskBuilder().delayTicks(delay).intervalTicks(interval).execute(runnable).submit(plugin));
    }

    @Override
    public UUID runAsynchronousTask(Module module, Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(module, runnable, 0);
    }

    @Override
    public UUID runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");
        return addTaskId(module, scheduler.createTaskBuilder().async().delay(delay * 50, MILLISECONDS).execute(runnable).submit(plugin));
    }

    private UUID addTaskId(Module module, Task task)
    {
        final Set<UUID> tasks = this.getModuleIDs(module);
        tasks.add(task.getUniqueId());
        return task.getUniqueId();
    }

    @Override
    public UUID runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        expectNotNull(module, "The module must not be null!");
        expectNotNull(runnable, "The runnable must not be null!");

        return addTaskId(module, scheduler.createTaskBuilder().async().delay(delay * 50, MILLISECONDS).interval(interval * 50, MILLISECONDS).execute(runnable).submit(plugin));
    }

    @Override
    public void cancelTask(Module module, UUID uuid)
    {
        Optional<Task> task = scheduler.getTaskById(uuid);
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
