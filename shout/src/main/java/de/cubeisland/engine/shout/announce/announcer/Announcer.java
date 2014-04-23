/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.shout.announce.announcer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Class to manage announcers. It's bound to system time, not the server.
 * This class does not time the tasks, it will only schedule them at the supplied delay.
 * Dynamic and fixed tasks are only separated for convenience, and is treated equal.
 */
public class Announcer
{
    private final ThreadFactory threadFactory;
    private ScheduledExecutorService executor;
    private Map<String, ScheduledFuture> dynamicTasks;
    private Map<String, ScheduledFuture> fixedTasks;
    public final int initDelay;

    public Announcer(ThreadFactory factory, int initDelay)
    {
        this.threadFactory = factory;
        this.executor = Executors.newSingleThreadScheduledExecutor(factory);
        this.dynamicTasks = new ConcurrentHashMap<>();
        this.fixedTasks = new ConcurrentHashMap<>();
        this.initDelay = initDelay;
    }

    /**
     * Schedule a dynamic time task.
     * A dynamic time task is run after the last task and will display at different times for each player.
     *
     * @param   id       The identification of the task, could be the name of the receiver
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleDynamicTask(String id, Runnable task, long delay)
    {
        this.dynamicTasks.put(id, this.executor
            .scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Schedule a fixed time task.
     * A fixed time task will display at the same time for all users.
     *
     * @param   id       The identification of the task, could be the name of the announcement
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleFixedTask(String id, Runnable task, long delay)
    {
        this.fixedTasks.put(id, this.executor
            .scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Cancels a dynamic time task.
     *
     * @param id The identification of the task to stop
     */
    public void cancelDynamicTask(String id)
    {
        ScheduledFuture future = this.dynamicTasks.remove(id);
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * Cancels a fixed time task.
     *
     * @param id The identification of the task to stop
     */
    public void cancelFixedTask(String id)
    {
        ScheduledFuture future = this.fixedTasks.remove(id);
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * Cancel all tasks and shut down the internal executor
     */
    public void shutdown()
    {
        for (String receiver : this.dynamicTasks.keySet())
        {
            this.cancelDynamicTask(receiver);
        }
        for (String name : this.fixedTasks.keySet())
        {
            this.cancelFixedTask(name);
        }
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
            this.executor.shutdownNow();
        }
        catch (InterruptedException ignore)
        {}
        this.dynamicTasks = null;
        this.fixedTasks = null;
    }

    /**
     * Cancel all tasks and replace the executor with a new one.
     */
    public void restart()
    {
        this.shutdown();
        this.executor = Executors.newSingleThreadScheduledExecutor(this.threadFactory);
        this.dynamicTasks = new ConcurrentHashMap<>();
        this.fixedTasks = new ConcurrentHashMap<>();
    }
}
