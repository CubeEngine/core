package de.cubeisland.cubeengine.core.util;

import java.lang.Runnable;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Worker
{
    private Queue<Runnable> jobs;
    private boolean pause;
    private boolean running;
    
    public void worker()
    {
        this.jobs = new LinkedList<Runnable>();
        this.pause = false;
        this.running = false;
    }
    
    public void shedule()
    {
        this.running = true;
        while(!(this.jobs.isEmpty() && pause))
        {
            this.jobs.poll().run();
        }
        this.running = false;
    }
    
    public void addJob(Runnable job)
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
