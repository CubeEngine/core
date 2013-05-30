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
package de.cubeisland.cubeengine.shout.announce.announcer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.cubeisland.cubeengine.core.task.TaskManager;

/**
 * Class to manage dynamicTasks based on the system time.
 */
public class Announcer
{
    private final TaskManager taskManager;
    private ScheduledExecutorService executor;
    private Map<String, ScheduledFuture> dynamicTasks;
    private Map<String, ScheduledFuture> fixedTasks;
    public int initDelay;

    public Announcer(TaskManager taskManager, int initDelay)
    {
        this.taskManager = taskManager;
        this.executor = Executors.newSingleThreadScheduledExecutor(taskManager.getThreadFactory());
        this.dynamicTasks = new ConcurrentHashMap<String, ScheduledFuture>();
        this.fixedTasks = new ConcurrentHashMap<String, ScheduledFuture>();
        this.initDelay = initDelay;
    }

    /**
     * Schedule a task based on the system time.
     *
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleDynamicTask(String receiver, Runnable task, long delay)
    {
        this.dynamicTasks.put(receiver, this.executor
            .scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Schedule a task based on the system time.
     *
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleFixedTask(String announcement, Runnable task, long delay)
    {
        this.fixedTasks.put(announcement, this.executor
            .scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param receiver the receiver the task should be stopped for
     */
    public void cancelDynamicTask(String receiver)
    {
        ScheduledFuture future = this.dynamicTasks.remove(receiver);
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param name the receiver the task should be stopped for
     */
    public void cancelFixedTask(String name)
    {
        ScheduledFuture future = this.fixedTasks.remove(name);
        if (future != null)
        {
            future.cancel(false);
        }
    }

    /**
     * Stop all announcements to all receivers
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

    public void restart()
    {
        this.shutdown();
        this.executor = Executors.newSingleThreadScheduledExecutor(taskManager.getThreadFactory());
        this.dynamicTasks = new ConcurrentHashMap<String, ScheduledFuture>();
        this.fixedTasks = new ConcurrentHashMap<String, ScheduledFuture>();
    }
}
