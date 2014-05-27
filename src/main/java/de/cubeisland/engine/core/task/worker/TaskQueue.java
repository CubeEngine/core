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
package de.cubeisland.engine.core.task.worker;

import de.cubeisland.engine.core.contract.NotNull;

/**
 * This interface is used for queuing tasks.
 */
public interface TaskQueue
{
    /**
     * Adds the runnable to this task queue.
     * Which will execute the runnable as soon as possible.
     *
     * @param runnable the runnable to enqueue
     */
    void addTask(@NotNull Runnable runnable);

    /**
     * Starts to execute the queued tasks.
     */
    void start();

    /**
     * Stops to execute the queued tasks.
     */
    void stop();

    /**
     * Shuts the task queue permanently down.
     */
    void shutdown();

    /**
     * Checks whether this task queue is shut down.
     *
     * @return true if the queue is shut down, false otherwise
     */
    boolean isShutdown();

    /**
     * Stops the execution of this queue.
     *
     * @param interrupt whether to interrupt the executor if it is running
     */
    void stop(boolean interrupt);

    /**
     * Returns whether the task queue is now running.
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
