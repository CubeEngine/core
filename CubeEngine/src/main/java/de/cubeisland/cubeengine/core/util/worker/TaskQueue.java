package de.cubeisland.cubeengine.core.util.worker;

/**
 *
 * @author Phillip Schichtel
 */
public interface TaskQueue
{
    void addTask(Runnable runnable);
    void start();
    void stop();
    boolean isRunning();
    int size();
}
