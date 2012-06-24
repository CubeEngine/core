package de.cubeisland.cubeengine.core.util;

import java.lang.Runnable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class SyncWorker implements Runnable
{
    private ConcurrentLinkedQueue<Runnable> jobs;
    private boolean paused;
    
    
    public void syncWorker(BukkitScheduler scheduler, Plugin core)
    {
        scheduler.scheduleSyncRepeatingTask(core, this, 1, 1);
        this.jobs = new ConcurrentLinkedQueue<Runnable>();
        this.paused = false;
    }
    
    @Override
    public void run()
    {
        if(!this.jobs.isEmpty() && !this.paused)
        {
            this.jobs.poll().run();
        }
    }
    
    public void addJob(Runnable job, boolean autoStart)
    {
        this.jobs.add(job);
        if(autoStart && paused)
        {
            this.resume();
        }
    }
    
    public void addJob(Runnable job)
    {
        this.jobs.add(job);
        if(paused)
        {
            this.resume();
        }
    }
    
    public void resume()
    {
        this.paused = false;
    }
    
    public void pause()
    {
        this.paused = true;
    }
    
    public void dropJobs()
    {
        this.pause();
        this.jobs.clear();
    }
}