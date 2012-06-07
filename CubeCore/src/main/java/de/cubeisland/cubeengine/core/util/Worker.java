package de.cubeisland.cubeengine.core.util;

import java.lang.Runnable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Worker implements Runnable
{
    private ConcurrentLinkedQueue<Runnable> jobs;
    private boolean running;
    private boolean paused;
    private Thread runner;
    
    public void worker()
    {
        this.jobs = new  ConcurrentLinkedQueue<Runnable>();
        this.running = false;
        this.paused = false;
        this.runner = new Thread(this);
    }
    
    @Override
    public synchronized void run()
    {
        this.running = true;
        while(this.jobs.isEmpty())
        {
            this.jobs.poll().run();
        }
        this.running = false;
    }
    
    public synchronized void addJob(Runnable job, boolean autoStart)
    {
        this.jobs.add(job);
        if(!running && autoStart)
        {
            this.runner.start();
        }
    }
    
    public synchronized boolean pause()
    {
        try
        {
            this.runner.wait();
            this.paused = true;
            return true;
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            return false;
        }
    }
    
    public synchronized void resume()
    {
        this.runner.notify();
        this.paused = false;
    }
    
    public void dropJobs()
    {
        this.jobs.clear();
    }
}
