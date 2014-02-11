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
package de.cubeisland.engine.core.task.worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This TaskQueue will execute all tasks in an async thread.
 */
public class AsyncTaskQueue implements TaskQueue
{
    private final Worker workerTask = new Worker();
    private final ExecutorService executorService;
    private final Queue<Runnable> taskQueue;
    private final AtomicReference<Future<?>> executorFuture;
    private boolean isShutdown;

    public AsyncTaskQueue(ExecutorService executorService)
    {
        this(executorService, new ConcurrentLinkedQueue<Runnable>());
    }

    public AsyncTaskQueue(ExecutorService executorService, Queue<Runnable> taskQueue)
    {
        this.executorService = executorService;
        this.taskQueue = taskQueue;
        this.executorFuture = new AtomicReference<>();
        this.isShutdown = false;
    }

    @Override
    public void addTask(Runnable runnable)
    {
        if (this.isShutdown)
        {
            return;
        }
        expectNotNull(runnable, "The task must not be null!");

        this.taskQueue.offer(runnable);
        this.start();
    }

    @Override
    public void start()
    {
        if (this.isShutdown)
        {
            throw new IllegalArgumentException("This task queue has been shut down!");
        }
        if (!this.isRunning())
        {
            this.executorFuture.set(this.executorService.submit(this.workerTask));
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
    public void stop()
    {
        this.stop(false);
    }

    @Override
    public void stop(boolean interupt)
    {
        Future<?> future = this.executorFuture.get();
        if (future != null)
        {
            future.cancel(interupt);
            this.executorFuture.set(null);
        }
    }

    @Override
    public boolean isRunning()
    {
        Future<?> future = this.executorFuture.get();
        return future != null && !future.isDone();
    }

    @Override
    public int size()
    {
        return this.taskQueue.size();
    }

    private class Worker implements Runnable
    {
        @Override
        public void run()
        {
            Runnable task;
            while ((task = AsyncTaskQueue.this.taskQueue.poll()) != null)
            {
                task.run();
            }
            AsyncTaskQueue.this.executorFuture.set(null);
        }
    }
}
