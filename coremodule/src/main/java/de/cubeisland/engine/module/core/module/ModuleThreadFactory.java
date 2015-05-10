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
package de.cubeisland.engine.module.core.module;

import java.lang.Thread.UncaughtExceptionHandler;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.task.thread.BaseThreadFactory;
import de.cubeisland.engine.module.core.task.thread.LoggingThread;
import de.cubeisland.engine.logscribe.LogLevel;

public class ModuleThreadFactory extends BaseThreadFactory
{
    private final Log log;
    private final UncaughtExceptionHandler exceptionHandler;

    public ModuleThreadFactory(ThreadGroup threadGroup, Log log)
    {
        super(new ThreadGroup(threadGroup, CubeEngine.class.getSimpleName() + " - " + log.getId()),
              log.getClass().getPackage().getName());
        this.log = log;
        this.exceptionHandler = new UncaughtModuleExceptionHandler(log);
    }

    @Override
    public Thread newThread(Runnable r)
    {
        Thread t = super.newThread(r);
        t.setUncaughtExceptionHandler(new UncaughtModuleExceptionHandler(log));
        return t;
    }

    @Override
    protected Thread createThread(ThreadGroup threadGroup, Runnable r, String name)
    {
        final LoggingThread thread = new LoggingThread(threadGroup, r, name, log, LogLevel.TRACE);
        thread.setUncaughtExceptionHandler(this.exceptionHandler);
        return thread;
    }


    private static final class UncaughtModuleExceptionHandler implements UncaughtExceptionHandler
    {
        private final Log log;

        public UncaughtModuleExceptionHandler(Log log)
        {
            this.log = log;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e)
        {
            this.log.error("An uncaught exception occurred! This is a critical error and should be reported!");
            this.log.error("The thread: {}", t.getName());
            this.log.error("The error: {}", e.getLocalizedMessage(), e);
        }
    }
}
