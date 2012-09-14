package de.cubeisland.cubeengine.core.util.worker;

import de.cubeisland.cubeengine.core.CubeEngine;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import org.bukkit.plugin.Plugin;

public class TaskQueue implements Runnable
{
    private Queue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    private boolean running;
    private int taskID;

    @Override
    public void run()
    {
        if (tasks.isEmpty())
        {
            this.running = false;
            CubeEngine.getServer().getScheduler().cancelTask(this.taskID);
            return;
        }
        tasks.poll().run();
    }

    public void addTasks(Runnable runnable)
    {
        this.tasks.offer(runnable);
        this.start();
    }

    private void start()
    {
        if (!this.running)
        {
            this.running = true;
            this.taskID = CubeEngine.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)CubeEngine.getCore(), this, 0, 1);
        }
    }
}