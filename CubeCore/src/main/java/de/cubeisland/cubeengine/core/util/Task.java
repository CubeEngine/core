package de.cubeisland.cubeengine.core.util;

import org.bukkit.plugin.Plugin;

/**
 * This Task can be cancelled from the inside
 * 
 * @author Faithcaio
 */
public abstract class Task implements Runnable {
    private int taskid;
    private Plugin plugin;
    public Task(Plugin plugin) {
        this.plugin = plugin;
    }
    public void setTaskId(int taskid) {
        this.taskid = taskid;
    }
    public void cancelTask() {
        plugin.getServer().getScheduler().cancelTask(taskid);
    }
    
}
