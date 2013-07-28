package de.cubeisland.engine.core.util;


import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.task.TaskManager;

public class FreezeDetection
{
    private final Core core;
    private final TaskManager taskManager;
    private ScheduledExecutorService executor;
    private int taskId;
    private long lastHeartbeat;
    private ConcurrentLinkedQueue<Runnable> listeners;

    public FreezeDetection(Core core)
    {
        this.core = core;
        this.taskManager = this.core.getTaskManager();
        this.executor = null;
        this.taskId = -1;
        this.lastHeartbeat = -1;
    }

    public void addListener(Runnable r)
    {
        this.listeners.add(r);
    }

    public void removeListener(Runnable r)
    {
        this.listeners.remove(r);
    }

    public void start()
    {
        this.taskId = this.taskManager.runAsynchronousTimer(core.getModuleManager().getCoreModule(), new HeartbeatLogger(), 0, 1);
        if (this.taskId == -1)
        {
            throw new RuntimeException("Failed to schedule the heartbeat logger for freeze detection");
        }
        this.executor = Executors.newSingleThreadScheduledExecutor(core.getTaskManager().getThreadFactory());
        this.executor.scheduleAtFixedRate(new FreezeDetector(), 30, 30, TimeUnit.SECONDS);

        this.lastHeartbeat = System.currentTimeMillis();
    }

    public void shutdown()
    {
        if (this.taskId != -1)
        {
            this.taskManager.cancelTask(this.core.getModuleManager().getCoreModule(), this.taskId);
            this.taskId = -1;
        }
        if (this.executor != null)
        {
            this.executor.shutdown();
            try
            {
                this.executor.awaitTermination(2, TimeUnit.SECONDS);
            }
            catch (InterruptedException ignored)
            {}
            this.executor = null;
        }

        this.listeners.clear();
    }

    private class HeartbeatLogger implements Runnable
    {
        @Override
        public void run()
        {
            lastHeartbeat = System.currentTimeMillis();
        }
    }

    private class FreezeDetector implements Runnable
    {
        @Override
        public void run()
        {
            if (System.currentTimeMillis() - lastHeartbeat > 1000 * 20)
            {
                Iterator<Runnable> it = listeners.iterator();
                while (it.hasNext())
                {
                    it.next().run();
                }
            }
        }
    }
}
