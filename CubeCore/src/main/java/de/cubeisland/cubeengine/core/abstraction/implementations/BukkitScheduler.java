package de.cubeisland.cubeengine.core.abstraction.implementations;

import de.cubeisland.cubeengine.core.abstraction.Plugin;
import de.cubeisland.cubeengine.core.abstraction.Scheduler;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 * @author CodeInfection
 */
public class BukkitScheduler implements Scheduler
{
    private final org.bukkit.scheduler.BukkitScheduler scheduler;

    public BukkitScheduler(org.bukkit.scheduler.BukkitScheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    public <T> Future<T> callSyncMethod(Plugin plugin, Callable<T> task)
    {
        return this.scheduler.callSyncMethod(((BukkitPlugin)plugin).getHandle(), task);
    }

    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task)
    {
        return this.scheduleAsyncDelayedTask(plugin, task, 0);
    }

    public int scheduleAsyncDelayedTask(Plugin plugin, Runnable task, long delay)
    {
        return this.scheduleAsyncRepeatingTask(plugin, task, 0, -1);
    }

    public int scheduleAsyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period)
    {
        return this.scheduler.scheduleAsyncRepeatingTask(((BukkitPlugin)plugin).getHandle(), task, delay, period);
    }

    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task)
    {
        return this.scheduleSyncDelayedTask(plugin, task, 0);
    }

    public int scheduleSyncDelayedTask(Plugin plugin, Runnable task, long delay)
    {
        return this.scheduleSyncRepeatingTask(plugin, task, 0, -1);
    }

    public int scheduleSyncRepeatingTask(Plugin plugin, Runnable task, long delay, long period)
    {
        return this.scheduler.scheduleSyncRepeatingTask(((BukkitPlugin)plugin).getHandle(), task, delay, period);
    }

    public void cancelTask(int id)
    {
        this.scheduler.cancelTask(id);
    }

    public void cancelTasks(Plugin plugin)
    {
        this.scheduler.cancelTasks(((BukkitPlugin)plugin).getHandle());
    }

    public void cancelAllTasks()
    {
        this.scheduler.cancelAllTasks();
    }
}
