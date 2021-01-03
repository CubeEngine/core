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
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.api.util.Ticks;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SpongeTaskManager implements TaskManager
{
    private final Game game;
    private PluginContainer plugin;

    @Inject
    public SpongeTaskManager(Game game, PluginContainer plugin)
    {
        this.game = game;
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask runTask(Runnable runnable)
    {
        return this.runTaskDelayed(runnable, 0);
    }

    @Override
    public ScheduledTask runTaskDelayed(Runnable runnable, long delay)
    {
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = newTask().delay(Ticks.of(delay)).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimer(Runnable runnable, long delay, long interval)
    {
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = newTask().delay(Ticks.of(delay)).interval(Ticks.of(interval)).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runAsynchronousTask(Runnable runnable)
    {
        return this.runAsynchronousTaskDelayed(runnable, 0);
    }

    @Override
    public ScheduledTask runAsynchronousTaskDelayed(Runnable runnable, long delay)
    {
        checkNotNull(runnable, "The runnable must not be null!");
        final Task task = newTask().delay(delay * 50, MILLISECONDS).execute(runnable).build();
        return game.getAsyncScheduler().submit(task);
    }

    @Override
    public ScheduledTask runAsynchronousTimer(Runnable runnable, long delay, long interval)
    {
        checkNotNull(runnable, "The runnable must not be null!");

        final Task task = newTask().delay(delay * 50, MILLISECONDS).execute(runnable).interval(interval * 50, MILLISECONDS).build();
        return game.getAsyncScheduler().submit(task);
    }

    private Task.Builder newTask() {
        return Task.builder().plugin(plugin);
    }
}
