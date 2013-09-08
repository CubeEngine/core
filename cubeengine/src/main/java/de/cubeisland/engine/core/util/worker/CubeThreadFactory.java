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
package de.cubeisland.engine.core.util.worker;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import de.cubeisland.engine.core.Core;



public class CubeThreadFactory implements ThreadFactory
{
    private final AtomicInteger counter;
    private final ThreadGroup group;
    private final String basePackage;
    private final Core core;

    public CubeThreadFactory(String name, Core core)
    {
        this(new ThreadGroup(name), core);
    }

    public CubeThreadFactory(ThreadGroup group, Core core)
    {
        this.group = group;
        this.counter = new AtomicInteger(0);

        String[] packageParts = this.getClass().getPackage().getName().split("\\.");
        this.basePackage = packageParts[0] + '.' + packageParts[1] + '.' + packageParts[2];

        this.core = core;
    }

    private String getCaller()
    {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stacktrace.length; ++i)
        {
            if (stacktrace[i].getClassName().startsWith(this.basePackage))
            {
                return stacktrace[i].getClassName() + '.' + stacktrace[i].getMethodName() + "()#" + stacktrace[i].getLineNumber();
            }
        }
        return null;
    }

    @Override
    public Thread newThread(Runnable r)
    {
        String name = "CubeEngine - Thread #" + this.counter.incrementAndGet();
        final String caller = this.getCaller();
        if (caller != null)
        {
            name += " - " + caller;
        }
        core.getLog().debug("Creating thread: {}", name);
        return new CubeThread(this.group, r, name);
    }

    private final class CubeThread extends Thread
    {
        private CubeThread(ThreadGroup group, Runnable target, String name)
        {
            super(group, target, name);
        }

        @Override
        public void interrupt()
        {
            super.interrupt();
            core.getLog().debug("Interrupted thread: {}", this.getName());
        }
    }
}
