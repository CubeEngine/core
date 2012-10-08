package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.plugin.Plugin;

/**
 * This Task can be cancelled from the inside
 *
 * @author Anselm Brehme
 */
public abstract class Task implements Runnable
{
    private int taskid;
    private Plugin plugin;

    public Task(Plugin plugin)
    {
        this.plugin = plugin;
    }

    public Task(Module module)
    {
        this((Plugin)null); // TODO module tasks needed to be registered with CubeEngine, but still need to be managed independent
    }

    public void setTaskId(int taskid)
    {
        this.taskid = taskid;
    }

    public void cancelTask()
    {
        plugin.getServer().getScheduler().cancelTask(taskid);
    }

    public void scheduleAsyncRepeatingTask(int delay, int repeat)
    {
        taskid = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, delay, repeat);
    }

    public void scheduleAsyncTask(int delay)
    {
        taskid = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, this, delay);
    }
}