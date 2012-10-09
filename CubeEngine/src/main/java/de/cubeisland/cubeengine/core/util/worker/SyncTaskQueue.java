package de.cubeisland.cubeengine.core.util.worker;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.commons.lang.Validate;

public class SyncTaskQueue implements TaskQueue
{
    private final Worker workerTask = new Worker();
    
    private final TaskManager taskManager;
    private final Queue<Runnable> taskQueue;
    private int taskID;

    public SyncTaskQueue(TaskManager taskManager)
    {
        this(taskManager, new LinkedList<Runnable>());
    }
    
    public SyncTaskQueue(TaskManager taskManager, Queue<Runnable> taskQueue)
    {
        this.taskManager = taskManager;
        this.taskQueue = taskQueue;
        this.taskID = -1;
    }

    public synchronized void run()
    {
        if (this.taskQueue.isEmpty())
        {
            taskManager.cancelTask(this.taskID);
            return;
        }
        this.taskQueue.poll().run();
    }

    @Override
    public synchronized void addTask(Runnable runnable)
    {
        Validate.notNull(runnable, "The runnable must not be null!");

        this.taskQueue.offer(runnable);
        this.start();
    }

    @Override
    public synchronized void start()
    {
        if (!this.isRunning())
        {
            this.taskID = this.taskManager.scheduleSyncRepeatingTask(this.workerTask, 0, 1);
        }
    }
    
    @Override
    public synchronized void stop()
    {
        if (this.isRunning())
        {
            this.taskManager.cancelTask(this.taskID);
            this.taskID = -1;
        }
    }
    
    @Override
    public synchronized boolean isRunning()
    {
        return this.taskID > -1;
    }
    
    @Override
    public synchronized int size()
    {
        return this.taskQueue.size();
    }
    
    private class Worker implements Runnable
    {
        @Override
        public void run()
        {
            taskQueue.poll().run();
            if (taskQueue.isEmpty())
            {
                stop();
            }
        }
    }
}