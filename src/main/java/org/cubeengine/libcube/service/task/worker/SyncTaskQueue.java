/*
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
package org.cubeengine.libcube.service.task.worker;

import java.util.LinkedList;
import java.util.Queue;
import org.cubeengine.libcube.service.task.TaskManager;
import org.spongepowered.api.scheduler.ScheduledTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This TaskQueue will execute one task every serverTick.
 */
public class SyncTaskQueue implements TaskQueue
{
    private final Worker workerTask = new Worker();
    private final TaskManager tm;
    private final Queue<Runnable> taskQueue;
    private ScheduledTask task;
    private boolean isShutdown;

    public SyncTaskQueue(TaskManager tm)
    {
        this(tm, new LinkedList<>());
    }

    public SyncTaskQueue(TaskManager tm, Queue<Runnable> taskQueue)
    {
        this.tm = tm;
        this.taskQueue = taskQueue;
        this.task = null;
        this.isShutdown = false;
    }

    public synchronized void run()
    {
        if (this.taskQueue.isEmpty())
        {
            if (task != null) {
                task.cancel();
            }
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
        checkNotNull(runnable, "The runnable must not be null!");

        this.taskQueue.offer(runnable);
        this.start();
    }

    @Override
    public synchronized void start()
    {
        if (!this.isRunning())
        {
            this.task = this.tm.runTimer(this.workerTask, 0, 1);
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
            task.cancel();
            this.task = null;
        }
    }

    @Override
    public synchronized boolean isRunning()
    {
        return this.task != null && !task.isCancelled();
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
