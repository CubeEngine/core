package de.cubeisland.cubeengine.core.util.worker;

import java.util.ArrayList;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class WorkerPool
{
    private ArrayList<Worker> workers;
    private int maxWorkerCount;
    
    public WorkerPool(int maxWorkerCount)
    {
        this.maxWorkerCount = maxWorkerCount;
        this.workers = new ArrayList<Worker>();
    }
    
    public void addJob(Runnable job)
    {
        boolean jobDelivered = false;
        
        for(Worker worker : this.workers)
        {
            if(worker.getJobCount() == 0 && !jobDelivered)
            {
                worker.addJob(job, true);
                jobDelivered = true;
            }
            else if(worker.getJobCount() == 0 && jobDelivered)
            {
                this.workers.remove(worker);
            }
        }
        
        int workersCount = this.workers.size();
        
        if(!jobDelivered && workersCount < this.maxWorkerCount)
        {
            this.workers.add(new Worker());
            this.workers.get(workersCount).addJob(job, true);
            jobDelivered = true;
        }

        int smallest = this.workers.get(0).getJobCount();
        int smallestIndex = 0;
        int next;
        
        for(int i = 1; i < workersCount || !jobDelivered; ++i) // TODO check this!
        {
            next = this.workers.get(i).getJobCount();
            if(smallest > next)
            {
                smallest = next;
                smallestIndex = i;
            }
        }
        
        this.workers.get(smallestIndex).addJob(job, true);
    }
    
    public void resume()
    {
        for(Worker worker : this.workers)
        {
            worker.resume();
        }
    }
    
    public void pause()
    {
        for(Worker worker : this.workers)
        {
            worker.pause();
        }
    }
    
    //kills all jobs running in all workers
    public void stop()
    {
        for(Worker worker : this.workers)
        {
            worker.stop();
        }
    }
    
    public int getWorkerCount()
    {
        return this.workers.size();
    }
}
