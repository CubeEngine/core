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
import de.cubeisland.engine.core.task.worker.BaseThreadFactory;

public class ModuleThreadFactory extends BaseThreadFactory
{
    private final Module module;

    public ModuleThreadFactory(Module module)
    {
        super(new ThreadGroup(
            module.getCore().getTaskManager().getThreadFactory().getThreadGroup(),
            CubeEngine.class.getSimpleName() + " - " + module.getName()), module.getClass().getPackage().getName()
        );
        this.module = module;
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
        return new ModuleThread(threadGroup, r, name, this.module);
    }

    private static class ModuleThread extends Thread
    {
        private final Module module;

        public ModuleThread(ThreadGroup threadGroup, Runnable r, String name, Module module)
        {
            super(threadGroup, r, name);
            this.setUncaughtExceptionHandler(new UncaughtModuleExceptionHandler(module));
            this.module = module;
            this.module.getLog().debug("Creating thread: {}", name);
        }

        @Override
        public synchronized void start()
        {
            super.start();
            this.module.getLog().debug("Started thread: {}", this.getName());
        }


        @Override
        public void interrupt()
        {
            super.interrupt();
            this.module.getLog().debug("Interrupted thread: {}", this.getName());
        }
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
