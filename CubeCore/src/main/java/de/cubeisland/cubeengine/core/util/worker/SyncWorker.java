package de.cubeisland.cubeengine.core.util.worker;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SyncWorker implements Runnable
{
    private Queue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
    private Thread task = new Thread(this);
    private Thread main = Thread.currentThread();
    private long timeout;
    private boolean suspendOnTimeOut = false;

    public void setSuspendOnTimeOut(boolean b)
    {
        this.suspendOnTimeOut = b;
    }

    public void run()
    {
        if (tasks.isEmpty())
        {
            main.notify();
            task.interrupt();
            return;
        }
        tasks.poll().run();
        this.run();
    }

    public void doTasks()
    {
        if (task.isAlive())
        {
            task.resume();
        }
        else
        {
            task.start();
        }
        try
        {
            main.wait(timeout);
            if (suspendOnTimeOut)
            {
                task.suspend();
            }
            else
            {
                task.interrupt();
            }
        }
        catch (InterruptedException ex)
        {
        }
    }

    public void addTasks(Runnable runnable)
    {
        this.tasks.offer(runnable);
    }
}