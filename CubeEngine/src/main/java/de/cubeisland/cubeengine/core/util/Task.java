package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import de.cubeisland.cubeengine.core.module.Module;

/**
 * This Task can be cancelled from the inside.
 */
public abstract class Task implements Runnable
{
    private int taskid;
    private final Module module;
    private final TaskManager tm;

    public Task(Module module)
    {
        this.module = module;
        this.tm = module.getCore().getTaskManager();
    }

    /**
     * Cancels the task
     */
    public void cancelTask()
    {
        this.tm.cancelTask(this.module, this.taskid);
    }

    /**
     * Schedules the task async repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleAsyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.scheduleAsyncRepeatingTask(this.module, this, delay, repeat);
    }

    /**
     * Schedules the task async delayed
     *
     * @param delay the delay
     */
    public void scheduleAsyncTask(int delay)
    {
        this.taskid = this.tm.scheduleAsyncDelayedTask(this.module, this, delay);
    }

    /**
     * Schedules the task sync repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleSyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.scheduleSyncRepeatingTask(this.module, this, delay, repeat);
    }

    /**
     * Schedules the task sync delayed
     *
     * @param delay the delay
     */
    public void scheduleSyncTask(int delay)
    {
        this.taskid = this.tm.scheduleSyncDelayedTask(this.module, this, delay);
    }
}
