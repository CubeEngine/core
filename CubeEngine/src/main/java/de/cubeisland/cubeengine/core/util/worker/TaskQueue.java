package de.cubeisland.cubeengine.core.util.worker;

/**
 * This interface is used for enqueing tasks.
 */
public interface TaskQueue
{
    /**
     * Adds the runnable to this taskqueue. 
     * Which will execute the runnable as soon as possible.
     * 
     * @param runnable the runnable to enqueue
     */
    void addTask(Runnable runnable);
    
    /**
     * Starts to execute the queued tasks.
     */
    void start();
    /**
     * Stops to execute the queued tasks.
     */
    void stop();
    /**
     * Returns whether the taskqueue is now running.
     * 
     * @return the running state of this queue
     */
    boolean isRunning();
    /**
     * Returns the amount of tasks not executed yet.
     * 
     * @return the task-amount
     */
    int size();
}
