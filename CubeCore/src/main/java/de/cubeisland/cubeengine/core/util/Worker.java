package de.cubeisland.cubeengine.core.util;

import java.lang.Runnable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Worker
{
    private ConcurrentLinkedQueue<Runnable> jobs;
    private boolean pause;
    private boolean running;
    
    public void worker()
    {
        this.jobs = new  ConcurrentLinkedQueue<Runnable>();
        this.pause = false;
        this.running = false;
    }
    
    public synchronized void shedule()
    {
        this.running = true;
        while(!(this.jobs.isEmpty() && pause))
        {
            new Thread(this.jobs.poll()).start();
        }
        this.running = false;
    }
    
    public synchronized void addJob(Runnable job)
    {
        this.jobs.add(job);
        if(!running)
        {
            this.shedule();
        }
    }
    
    public void pauseWorker()
    {
        this.pause = true;
    }
    
    public void resume()
    {
        this.pause = false;
        this.shedule();
    }
}
