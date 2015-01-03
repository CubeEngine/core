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
package de.cubeisland.engine.core.task.thread;

import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;

public final class LoggingThread extends TrackedThread
{
    private final Log log;
    private final LogLevel level;

    public LoggingThread(ThreadGroup group, Runnable target, String name, Log log, LogLevel level)
    {
        super(group, target, name);
        this.log = log;
        this.level = level;
        this.log.log(level, "Creating thread: {}", name);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.log.log(level, "Starting thread: {}", getName());
    }

    @Override
    public void onStarted()
    {
        super.onStarted();
        this.log.log(level, "Started thread: {}", getName());
    }

    @Override
    public void onBeginExecution()
    {
        super.onBeginExecution();
        this.log.log(level, "Executing thread: {}", getName());
    }

    @Override
    public void onExecutionComplete()
    {
        super.onExecutionComplete();
        this.log.log(level, "Finished executing thread: {}", getName());
    }

    @Override
    public void onInterrupted()
    {
        super.onInterrupted();
        this.log.log(level, "Interrupted thread: {}", getName());
    }
}
