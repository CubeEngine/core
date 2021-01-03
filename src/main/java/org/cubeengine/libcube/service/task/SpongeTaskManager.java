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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.Game;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.api.util.Ticks;

import java.time.Duration;
import java.util.function.Consumer;

@Singleton
public class SpongeTaskManager implements TaskManager
{
    private final Game game;
    private final PluginContainer plugin;

    @Inject
    public SpongeTaskManager(Game game, PluginContainer plugin)
    {
        this.game = game;
        this.plugin = plugin;
    }

    @Override
    public ScheduledTask runTask(Runnable runnable)
    {
        final Task task = newTask().execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTaskDelayed(Runnable runnable, Duration delay)
    {
        final Task task = newTask().delay(delay).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTaskDelayed(Runnable runnable, Ticks delay)
    {
        final Task task = newTask().delay(delay).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTaskAsync(Runnable runnable)
    {
        final Task task = newTask().execute(runnable).build();
        return game.getAsyncScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTaskAsyncDelayed(Runnable runnable, Duration delay)
    {
        final Task task = newTask().delay(delay).execute(runnable).build();
        return game.getAsyncScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTaskAsyncDelayed(Runnable runnable, Ticks delay)
    {
        final Task task = newTask().delay(delay).execute(runnable).build();
        return game.getAsyncScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimer(Consumer<ScheduledTask> runnable, Duration delay, Duration interval)
    {
        final Task task = newTask().delay(delay).interval(interval).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimer(Consumer<ScheduledTask> runnable, Ticks delay, Ticks interval)
    {
        final Task task = newTask().delay(delay).interval(interval).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimer(Consumer<ScheduledTask> runnable, Duration interval)
    {
        final Task task = newTask().interval(interval).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimer(Consumer<ScheduledTask> runnable, Ticks interval)
    {
        final Task task = newTask().interval(interval).execute(runnable).build();
        return game.getServer().getScheduler().submit(task);
    }

    @Override
    public ScheduledTask runTimerAsync(Consumer<ScheduledTask> runnable, Ticks delay, Ticks interval)
    {
        final Task task = newTask().delay(delay).execute(runnable).interval(interval).build();
        return game.getAsyncScheduler().submit(task);
    }

    private Task.Builder newTask() {
        return Task.builder().plugin(plugin);
    }
}
