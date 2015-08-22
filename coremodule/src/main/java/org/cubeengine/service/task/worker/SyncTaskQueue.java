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
package org.cubeengine.service.task.worker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.task.TaskManager;

import static org.cubeengine.module.core.contract.Contract.expectNotNull;

/**
 * This TaskQueue will execute one task every serverTick.
 */
public class SyncTaskQueue implements TaskQueue
{
    private final Worker workerTask = new Worker();
    private final CoreModule corePlugin;
    private final TaskManager scheduler;
    private final Queue<Runnable> taskQueue;
    private UUID taskID;
    private boolean isShutdown;

    public SyncTaskQueue(CoreModule core)
    {
        this(core, new LinkedList<Runnable>());
    }

    public SyncTaskQueue(CoreModule core, Queue<Runnable> taskQueue)
    {
        this.corePlugin = core;
        this.scheduler = this.corePlugin.getModularity().provide(TaskManager.class);
        this.taskQueue = taskQueue;
        this.taskID = null;
        this.isShutdown = false;
    }

    public synchronized void run()
    {
        if (this.taskQueue.isEmpty())
        {
            this.scheduler.cancelTask(corePlugin, this.taskID);
            return;
        }
        this.taskQueue.poll().run();
    }

    @Override
    public synchronized void addTask(Runnable runnable)
    {
        if (!this.isShutdown)
        {
            return;
        }
        expectNotNull(runnable, "The runnable must not be null!");

        this.taskQueue.offer(runnable);
        this.start();
    }

    @Override
    public synchronized void start()
    {
        if (!this.isRunning())
        {
            this.taskID = this.scheduler.runTimer(corePlugin, this.workerTask, 0, 1);
        }
    }

    @Override
    public void shutdown()
    {
        this.isShutdown = true;
        this.taskQueue.clear();
        this.stop();
    }

    @Override
    public boolean isShutdown()
    {
        return this.isShutdown;
    }

    @Override
    public synchronized void stop()
    {
        this.stop(false);
    }

    @Override
    public synchronized void stop(boolean interupt)
    {
        if (this.isRunning())
        {
            this.scheduler.cancelTask(corePlugin, this.taskID);
            this.taskID = null;
        }
    }

    @Override
    public synchronized boolean isRunning()
    {
        return this.taskID != null;
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
