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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.cubeisland.cubeengine.core.bukkit.TaskManager;

/**
 * Class to manage futures based on the system time.
 */
public class Announcer
{
    private ScheduledExecutorService executor;
    private Map<String, ScheduledFuture> futures;
    public int initDelay;

    public Announcer(TaskManager taskManager, int initDelay)
    {
        this.executor = Executors.newSingleThreadScheduledExecutor(taskManager.getThreadFactory());
        this.futures = new HashMap<String, ScheduledFuture>();
        this.initDelay = initDelay;
    }

    /**
     * Schedule a task based on the system time.
     *
     * @param	task	 The task to schedule
     * @param	delay	 Delay between each time this task in run.
     */
    public void scheduleTask(String receiver, Runnable task, long delay)
    {
        this.futures.put(receiver, this.executor.scheduleAtFixedRate(task, this.initDelay, delay, TimeUnit.MILLISECONDS));
    }

    /**
     * Stops a receiver from receiving any more announcements
     *
     * @param receiver the receiver the task should be stopped for
     */
    public void cancel(String receiver)
    {
        ScheduledFuture future = this.futures.remove(receiver);
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
        this.futures.clear();
        this.executor.shutdown();
        try
        {
            this.executor.awaitTermination(1, TimeUnit.SECONDS);
            this.executor.shutdownNow();
        }
        catch (InterruptedException ignore)
        {}
        this.executor = null;
        this.futures = null;
    }
}
