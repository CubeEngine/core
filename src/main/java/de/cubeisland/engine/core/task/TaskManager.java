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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.ModuleThreadFactory;
import de.cubeisland.engine.core.task.thread.CoreThreadFactory;
import de.cubeisland.engine.core.util.Cleanable;

/**
 * This class provides methods to register and cancel tasks and the global
 * ScheduledExecutorService is provided by this class.
 */
public interface TaskManager extends Cleanable
{
    /**
     * Returns the thread factory used by the CubeEngine to create its threads
     *
     * @return a ThreadFactory implementation
     */
    CoreThreadFactory getThreadFactory();

    /**
     * Returns a new thread factory for a module
     *
     * @return a ThreadFactory implementation
     */
    ModuleThreadFactory getThreadFactory(Module module);


    /**
     * Schedules a delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    int runTask(Module module, Runnable runnable);

    /**
     * Schedules a delayed task for a module with the given delay on the main server thread
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    int runTaskDelayed(Module module, Runnable runnable, long delay);

    /**
     * Schedules a repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    int runTimer(Module module, Runnable runnable, long delay, long interval);

    /**
     * Schedules a asynchronous delayed task for a module
     *
     * @param module   the module
     * @param runnable the task
     * @return the ID of the task
     */
    int runAsynchronousTask(Module module, Runnable runnable);

    /**
     * Schedules a asynchronous delayed task for a module with the given delay
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    int runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay);

    /**
     * Schedules a asynchronous repeating task for a module with the given delay and interval
     *
     * @param module   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    int runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval);

    /**
     * Schedules a method for execution on the main server thread
     *
     * @param <T>      the return type of the method
     * @param callable the method to call
     * @return a future object
     */
    <T> Future<T> callSync(Callable<T> callable);

    /**
     * Cancels a task of a module
     *
     * @param module the module
     * @param ID     the taskID
     */
    void cancelTask(Module module, int ID);

    void cancelTasks(Module module);

    /**
     * Checks whether the given task ID revers to a task that's currently running
     *
     * @param taskID the task ID
     * @return true if there is a running task for this ID
     */
    boolean isCurrentlyRunning(int taskID);

    /**
     * Checks whether the given task ID is
     *
     * @param taskID the task ID
     * @return true if there is a task for this ID
     */
    boolean isQueued(int taskID);

    /**
     * This method can be used to remove all objects related to the given module
     *
     * @param module the module
     */
    void clean(Module module);
}
