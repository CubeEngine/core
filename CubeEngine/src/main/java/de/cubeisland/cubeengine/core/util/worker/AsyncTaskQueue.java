package de.cubeisland.cubeengine.core.util.worker;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.lang.Validate;

/**
 *
 * @author Phillip Schichtel
 */
public class AsyncTaskQueue implements TaskQueue
{
    private final Worker workerTask = new Worker();
    
    private Thread worker;
    private final BlockingQueue<Runnable> taskQueue;
    
    public AsyncTaskQueue()
    {
        this(new LinkedBlockingQueue<Runnable>());
    }

    public AsyncTaskQueue(BlockingQueue<Runnable> taskQueue)
    {
        this.worker = null;
        this.taskQueue = taskQueue;
    }
    
    @Override
    public void addTask(Runnable runnable)
    {
        Validate.notNull(runnable, "The task must not be null!");
        
        this.taskQueue.offer(runnable);
    }

    @Override
    public void start()
    {
        if (!this.isRunning())
        {
            this.worker = new Thread(this.workerTask, "AsyncTaskQueue");
        }
    }

    @Override
    public void stop()
    {
        this.worker = null;
    }

    @Override
    public boolean isRunning()
    {
        return worker != null;
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
            while (worker != null)
            {
                taskQueue.poll().run();
            }
        }
    }
}
