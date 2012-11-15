package de.cubeisland.cubeengine.core.util.worker;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.Validate;

/**
 * This TaskQueue will execute all tasks in an async thread.
 */
public class AsyncTaskQueue implements TaskQueue
{
    private final Worker                     workerTask = new Worker();
    private final ExecutorService            executorService;
    private final Queue<Runnable>            taskQueue;
    private final AtomicReference<Future<?>> exectorFuture;
    private boolean                          isShutdown;

    public AsyncTaskQueue(ExecutorService executorService)
    {
        this(executorService, new ConcurrentLinkedQueue<Runnable>());
    }

    public AsyncTaskQueue(ExecutorService executorService, Queue<Runnable> taskQueue)
    {
        this.executorService = executorService;
        this.taskQueue = taskQueue;
        this.exectorFuture = new AtomicReference<Future<?>>();
        this.isShutdown = false;
    }

    @Override
    public void addTask(Runnable runnable)
    {
        if (this.isShutdown)
        {
            return;
        }
        Validate.notNull(runnable, "The task must not be null!");

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
        if (this.exectorFuture.get() == null)
        {
            this.exectorFuture.set(this.executorService.submit(this.workerTask));
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
        Future<?> future = this.exectorFuture.get();
        if (future != null)
        {
            future.cancel(interupt);
            this.exectorFuture.set(null);
        }
    }

    @Override
    public boolean isRunning()
    {
        Future<?> future = this.exectorFuture.get();
        if (future != null)
        {
            return !future.isDone();
        }
        return false;
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
            AsyncTaskQueue.this.exectorFuture.set(null);
        }
    }
}
