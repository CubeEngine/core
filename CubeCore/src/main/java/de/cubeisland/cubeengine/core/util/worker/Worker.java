package de.cubeisland.cubeengine.core.util.worker;

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
    
    public Worker()
    {
        this.jobs = new ConcurrentLinkedQueue<Runnable>();
        this.running = false;
        this.paused = false;
        this.runner = new Thread(this);
    }
    
    @Override
    public void run()
    {
        this.running = true;
        while(this.jobs.isEmpty())
        {
            this.jobs.poll().run();
        }
        this.running = false;
    }
    
    public void addJob(Runnable job, boolean autoStart)
    {
        this.jobs.add(job);
        if(autoStart)
        {
            this.runner.start();
        }
    }
    
    public void start()
    {
        if(!running)
        {
            this.runner.start();
            this.paused = false;
        }
    }
    
    public void stop()
    {
        this.running = false;
        this.paused = false;
        this.jobs.clear();
    }
    
    public boolean pause()
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
    
    public void resume()
    {
        this.runner.notify();
        this.paused = false;
    }
    
    public void dropJobs()
    {
        this.jobs.clear();
    }
}
