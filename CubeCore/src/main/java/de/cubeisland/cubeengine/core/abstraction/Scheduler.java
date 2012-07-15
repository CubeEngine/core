package de.cubeisland.cubeengine.core.abstraction;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 *
 * @author CodeInfection
 */
public interface Scheduler
{
    public <T> Future<T> callSyncMethod(Plugin owner, Callable<T> task);

    public int scheduleAsyncDelayedTask(Plugin owner, Runnable task);

    public int scheduleAsyncDelayedTask(Plugin owner, Runnable task, long delay);

    public int scheduleAsyncRepeatingTask(Plugin owner, Runnable task, long delay, long period);

    public int scheduleSyncDelayedTask(Plugin owner, Runnable task);

    public int scheduleSyncDelayedTask(Plugin owner, Runnable task, long delay);

    public int scheduleSyncRepeatingTask(Plugin owner, Runnable task, long delay, long period);

    public void cancelTask(int id);

    public void cancelTasks(Plugin owner);

    public void cancelAllTasks();
}
