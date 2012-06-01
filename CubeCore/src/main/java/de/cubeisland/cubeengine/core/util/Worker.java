package de.cubeisland.cubeengine.core.util;

import java.lang.Runnable;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author Robin Bechtel-Ostmann
 */

public class Worker implements Runnable
{
    public void worker()
    {
        Queue<Runnable> jobs = new LinkedList<Runnable>();
    }
    
    public void run()
    {
        while(true)
        {
            
        }
    }
    
    public void pauseWorker()
    {
        
    }
    
    public void stopWorker()
    {
        
    }
}
