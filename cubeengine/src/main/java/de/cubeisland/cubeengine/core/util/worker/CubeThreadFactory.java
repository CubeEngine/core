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
package de.cubeisland.cubeengine.core.util.worker;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class CubeThreadFactory implements ThreadFactory
{
    private final AtomicInteger counter;
    private final ThreadGroup group;

    public CubeThreadFactory(String name)
    {
        this(new ThreadGroup(name));
    }

    public CubeThreadFactory(ThreadGroup group)
    {
        this.group = group;
        this.counter = new AtomicInteger(0);
    }

    @Override
    public Thread newThread(Runnable r)
    {
        final String name = "CubeEngine - Thread #" + this.counter.incrementAndGet();
        CubeEngine.getLog().log(DEBUG, "Creating thread: {0}", name);
        return new Thread(this.group, r, name);
    }
}
