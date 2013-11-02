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

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;

public class CoreThreadFactory extends BaseThreadFactory
{
    private final Core core;

    public CoreThreadFactory(Core core)
    {
        super(CubeEngine.class.getSimpleName(), CubeEngine.class.getPackage().getName());
        this.core = core;
    }

    @Override
    protected Thread createThread(ThreadGroup threadGroup, Runnable r, String name)
    {
        return new CoreThread(threadGroup, r, name, this.core);
    }

    private static class CoreThread extends Thread
    {
        private final Core core;

        public CoreThread(ThreadGroup threadGroup, Runnable r, String name, Core core)
        {
            super(threadGroup, r, name);
            this.core = core;
            this.core.getLog().debug("Creating thread: {}", name);
        }

        @Override
        public synchronized void start()
        {
            super.start();
            this.core.getLog().debug("Started thread: {}", this.getName());
        }

        @Override
        public void interrupt()
        {
            super.interrupt();
            this.core.getLog().debug("Interrupted thread: {}", this.getName());
        }
    }
}
