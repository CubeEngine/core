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
package de.cubeisland.engine.core.module;

import java.lang.Thread.UncaughtExceptionHandler;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.task.thread.BaseThreadFactory;
import de.cubeisland.engine.core.task.thread.LoggingThread;
import de.cubeisland.engine.logscribe.LogLevel;

public class ModuleThreadFactory extends BaseThreadFactory
{
    private final Module module;
    private final UncaughtExceptionHandler exceptionHandler;

    public ModuleThreadFactory(Module module)
    {
        super(new ThreadGroup(
            module.getCore().getTaskManager().getThreadFactory().getThreadGroup(),
            CubeEngine.class.getSimpleName() + " - " + module.getName()), module.getClass().getPackage().getName()
        );
        this.module = module;
        this.exceptionHandler = new UncaughtModuleExceptionHandler(module);
    }

    @Override
    public Thread newThread(Runnable r)
    {
        Thread t = super.newThread(r);
        t.setUncaughtExceptionHandler(new UncaughtModuleExceptionHandler(this.module));
        return t;
    }

    @Override
    protected Thread createThread(ThreadGroup threadGroup, Runnable r, String name)
    {
        final LoggingThread thread = new LoggingThread(threadGroup, r, name, this.module.getLog(), LogLevel.TRACE);
        thread.setUncaughtExceptionHandler(this.exceptionHandler);
        return thread;
    }


    private static final class UncaughtModuleExceptionHandler implements UncaughtExceptionHandler
    {
        private final Module module;

        public UncaughtModuleExceptionHandler(Module module)
        {
            this.module = module;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e)
        {
            this.module.onUncaughtException(t, e);
        }
    }
}
