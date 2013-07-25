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
package de.cubeisland.engine.core.task;

import de.cubeisland.engine.core.module.Module;

/**
 * This Task can be cancelled from the inside.
 */
public abstract class Task implements Runnable
{
    private int taskid;
    private final Module module;
    private final TaskManager tm;

    public Task(Module module)
    {
        this.module = module;
        this.tm = module.getCore().getTaskManager();
    }

    /**
     * Cancels the task
     */
    public void cancelTask()
    {
        this.tm.cancelTask(this.module, this.taskid);
    }

    /**
     * Schedules the task async repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleAsyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.runAsynchronousTimer(this.module, this, delay, repeat);
    }

    /**
     * Schedules the task async delayed
     *
     * @param delay the delay
     */
    public void scheduleAsyncTask(int delay)
    {
        this.taskid = this.tm.runAsynchronousTaskDelayed(this.module, this, delay);
    }

    /**
     * Schedules the task sync repeating
     *
     * @param delay  the delay
     * @param repeat the interval
     */
    public void scheduleSyncRepeatingTask(int delay, int repeat)
    {
        this.taskid = this.tm.runTimer(this.module, this, delay, repeat);
    }

    /**
     * Schedules the task sync delayed
     *
     * @param delay the delay
     */
    public void scheduleSyncTask(int delay)
    {
        this.taskid = this.tm.runTaskDelayed(this.module, this, delay);
    }
}
