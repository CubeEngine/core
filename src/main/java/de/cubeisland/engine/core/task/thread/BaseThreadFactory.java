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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseThreadFactory implements ThreadFactory
{
    private final AtomicInteger threadCounter;
    private final ThreadGroup threadGroup;
    private volatile boolean enabled;
    private final String basePackage;

    protected BaseThreadFactory(String name, String basePackage)
    {
        this(new ThreadGroup(name), basePackage);
    }

    protected BaseThreadFactory(ThreadGroup threadGroup, String basePackage)
    {
        this.threadGroup = threadGroup;
        this.threadCounter = new AtomicInteger(0);
        this.enabled = true;
        this.basePackage = basePackage;
    }

    public ThreadGroup getThreadGroup()
    {
        return this.threadGroup;
    }

    private String getSource(int skip)
    {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 0)
        {
            int i = 0;
            for (StackTraceElement e : trace)
            {
                if (i++ < skip)
                {
                    continue;
                }
                if (e.getClassName().startsWith(this.basePackage))
                {
                    return stackTraceElementToString(e);
                }
            }
            return stackTraceElementToString(trace[skip]);
        }
        return "unknown source";
    }

    private static String stackTraceElementToString(StackTraceElement e)
    {
        return e.getClassName() + '.' + e.getMethodName() + "(" + e.getFileName() + ":" + e.getLineNumber() + ")";
    }

    @Override
    public Thread newThread(Runnable r)
    {
        if (!this.enabled)
        {
            throw new IllegalStateException("This thread factory is already shut down!");
        }
        return this.createThread(this.threadGroup, r, this.constructName());
    }

    protected abstract Thread createThread(ThreadGroup threadGroup, Runnable r, String name);

    protected String constructName()
    {
        return this.threadGroup.getName() + " - Thread #" + this.threadCounter.incrementAndGet() + " - " + this.getSource(5);
    }

    public void shutdown()
    {
        this.enabled = false;
    }
}
