/**
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
    private final long freezeThreshold;
    private final ConcurrentLinkedQueue<Runnable> listeners = new ConcurrentLinkedQueue<>();
    private volatile boolean freezeNotified = false;

    public FreezeDetection(Core core, long freezeThreshold)
    {
        this(core, freezeThreshold, TimeUnit.SECONDS);
    }

    public FreezeDetection(Core core, long freezeThreshold, TimeUnit unit)
    {
        this.core = core;
        this.taskManager = this.core.getTaskManager();
        this.executor = null;
        this.taskId = -1;
        this.lastHeartbeat = -1;
        this.freezeThreshold = unit.toMillis(freezeThreshold);
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
            throw new RuntimeException("Failed to schedule the heartbeat logging for freeze detection");
        }
        this.executor = Executors.newSingleThreadScheduledExecutor(core.getTaskManager().getThreadFactory());
        this.executor.scheduleAtFixedRate(new FreezeDetector(), this.freezeThreshold, this.freezeThreshold, TimeUnit.MILLISECONDS);

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
            freezeNotified = false;
        }
    }

    private class FreezeDetector implements Runnable
    {
        @Override
        public void run()
        {
            if (System.currentTimeMillis() - lastHeartbeat > freezeThreshold && !freezeNotified)
            {
                freezeNotified = true;
                Iterator<Runnable> it = listeners.iterator();
                while (it.hasNext())
                {
                    it.next().run();
                }
            }
        }
    }
}
