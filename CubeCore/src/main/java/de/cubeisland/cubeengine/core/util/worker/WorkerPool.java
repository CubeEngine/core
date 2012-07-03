package de.cubeisland.cubeengine.core.util.worker;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class WorkerPool
{
    private Worker[] workers;
    private int workerCount;
    
    public WorkerPool(int workerCount)
    {
        this.workerCount = workerCount;
        this.workers = new Worker[this.workerCount];
        
        for(int i = 0; i < this.workerCount; i++)
        {
            this.workers[i] = new Worker();
        }
    }
    
    public void addJob(Runnable job)
    {
        int smallest = this.workers[0].getJobCount();
        int smallestIndex = 0;
        int next;
        
        for(int i = 1; i < this.workerCount; i++)
        {
            next = this.workers[i].getJobCount();
            if(smallest > next)
            {
                smallest = next;
                smallestIndex = i;
            }
        }
        
        this.workers[smallestIndex].addJob(job, true);
    }
    
    public void resume()
    {
        for(int i = 1; i < this.workerCount; i++)
        {
            this.workers[i].resume();
        }
    }
    
    public void pause()
    {
        for(int i = 1; i < this.workerCount; i++)
        {
            this.workers[i].pause();
        }
    }
    
    //kills all jobs running in all workers
    public void stop()
    {
        for(int i = 1; i < this.workerCount; i++)
        {
            this.workers[i].stop();
        }
    }
    
    public int getWorkerCount()
    {
        return this.workerCount;
    }
}
