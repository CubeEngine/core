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
package org.cubeengine.libcube.service.task;

import java.util.UUID;
import de.cubeisland.engine.modularity.asm.marker.Service;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;

/**
 * This class provides methods to register and cancel tasks and the global
 * ScheduledExecutorService is provided by this class.
 */
@Service
@Version(1)
public interface TaskManager
{

    /**
     * Schedules a delayed task for a module
     *
     * @param owner   the module
     * @param runnable the task
     * @return the ID of the task
     */
    UUID runTask(Class owner, Runnable runnable);

    /**
     * Schedules a delayed task for a module with the given delay on the main server thread
     *
     * @param owner   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    UUID runTaskDelayed(Class owner, Runnable runnable, long delay);

    /**
     * Schedules a repeating task for a module with the given delay and interval
     *
     * @param owner   the module
     * @param runnable the task
     * @param delay    the delay in ticks in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    UUID runTimer(Class owner, Runnable runnable, long delay, long interval);

    /**
     * Schedules a asynchronous delayed task for a module
     *
     * @param owner   the module
     * @param runnable the task
     * @return the ID of the task
     */
    UUID runAsynchronousTask(Class owner, Runnable runnable);

    /**
     * Schedules a asynchronous delayed task for a module with the given delay
     *
     * @param owner   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @return the ID of the task
     */
    UUID runAsynchronousTaskDelayed(Class owner, Runnable runnable, long delay);

    /**
     * Schedules a asynchronous repeating task for a module with the given delay and interval
     *
     * @param owner   the module
     * @param runnable the task
     * @param delay    the delay in ticks
     * @param interval the interval in ticks
     * @return the ID of the task
     */
    UUID runAsynchronousTimer(Class owner, Runnable runnable, long delay, long interval);

    /**
     * Cancels a task of a module
     *
     * @param owner the module
     * @param uuid the taskID
     */
    void cancelTask(Class owner, UUID uuid);

    void cancelTasks(Class owner);

    /**
     * This method can be used to remove all objects related to the given module
     *
     * @param owner the module
     */
    void clean(Class owner);
}
