/*
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
package org.cubeengine.libcube.service.task;

import java.util.UUID;

/**
 * This Task can be cancelled from the inside.
 */
public abstract class Task implements Runnable
{
    private UUID taskid;
    private final Class owner;
    private final TaskManager tm;

    public Task(Class owner, TaskManager tm)
    {
        this.owner = owner;
        this.tm = tm;
    }

    /**
     * Cancels the task
     */
    public void cancelTask()
    {
        this.tm.cancelTask(this.owner, this.taskid);
    }

    /**
     * Schedules the task async repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleAsyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.runAsynchronousTimer(this.owner, this, delay, repeat);
    }

    /**
     * Schedules the task async delayed
     *
     * @param delay the delay
     */
    public void scheduleAsyncTask(int delay)
    {
        this.taskid = this.tm.runAsynchronousTaskDelayed(this.owner, this, delay);
    }

    /**
     * Schedules the task sync repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleSyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.runTimer(this.owner, this, delay, repeat);
    }

    /**
     * Schedules the task sync delayed
     *
     * @param delay the delay
     */
    public void scheduleSyncTask(int delay)
    {
        this.taskid = this.tm.runTaskDelayed(this.owner, this, delay);
    }
}
