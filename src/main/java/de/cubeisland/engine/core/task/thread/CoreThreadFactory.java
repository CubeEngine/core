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

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.logscribe.LogLevel;

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
        return new LoggingThread(threadGroup, r, name, this.core.getLog(), LogLevel.TRACE);
    }
}
