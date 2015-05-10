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
package de.cubeisland.engine.module.core.task;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import com.google.common.base.Optional;
import de.cubeisland.engine.module.core.module.Module;
import de.cubeisland.engine.module.core.module.ModuleThreadFactory;
import de.cubeisland.engine.module.core.task.thread.CoreThreadFactory;

public class TestTaskManager implements TaskManager
{
    @Override
    public CoreThreadFactory getThreadFactory()
    {
        return null;
    }

    @Override
    public ModuleThreadFactory getThreadFactory(Module module)
    {
        return null;
    }

    @Override
    public Optional<UUID> runTask(Module module, Runnable runnable)
    {
        return 0;
    }

    @Override
    public Optional<UUID> runTaskDelayed(Module module, Runnable runnable, long delay)
    {
        return 0;
    }

    @Override
    public Optional<UUID> runTimer(Module module, Runnable runnable, long delay, long interval)
    {
        return 0;
    }

    @Override
    public Optional<UUID> runAsynchronousTask(Module module, Runnable runnable)
    {
        return 0;
    }

    @Override
    public Optional<UUID> runAsynchronousTaskDelayed(Module module, Runnable runnable, long delay)
    {
        return 0;
    }

    @Override
    public Optional<UUID> runAsynchronousTimer(Module module, Runnable runnable, long delay, long interval)
    {
        return 0;
    }

    @Override
    public <T> Future<T> callSync(Callable<T> callable)
    {
        return null;
    }

    @Override
    public void cancelTask(Module module, UUID uuid)
    {

    }

    @Override
    public void cancelTasks(Module module)
    {

    }

    @Override
    public boolean isCurrentlyRunning(UUID taskID)
    {
        return false;
    }

    @Override
    public boolean isQueued(UUID taskID)
    {
        return false;
    }

    @Override
    public void clean(Module module)
    {

    }

    @Override
    public void clean()
    {

    }
}
