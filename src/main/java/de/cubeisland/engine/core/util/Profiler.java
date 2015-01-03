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
package de.cubeisland.engine.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This class helps profiling code
 */
public class Profiler
{
    private static final Map<String, Long> startTimes = new HashMap<>();

    public static void startProfiling(String id)
    {
        final long nanos = System.nanoTime();
        synchronized (startTimes)
        {
            if (startTimes.containsKey(id))
            {
                throw new IllegalStateException("This ID is already being profiled!");
            }
            startTimes.put(id, nanos);
        }
    }

    public static long getCurrentDelta(String id)
    {
        final long nanos = System.nanoTime();
        expectNotNull(id, "The ID must not be null!");
        synchronized (startTimes)
        {
            if (!startTimes.containsKey(id))
            {
                throw new IllegalStateException("This ID is not being profiled!");
            }
            return nanos - startTimes.get(id);
        }
    }

    public static long getCurrentDelta(String id, TimeUnit unit)
    {
        return unit.convert(getCurrentDelta(id), TimeUnit.NANOSECONDS);
    }

    public static long endProfiling(String id)
    {
        final long delta = System.nanoTime();
        expectNotNull(id, "The ID must not be null!");
        synchronized (startTimes)
        {
            return delta - startTimes.remove(id);
        }
    }

    public static long endProfiling(String id, TimeUnit unit)
    {
        return unit.convert(endProfiling(id), TimeUnit.NANOSECONDS);
    }

    public static void clean()
    {
        synchronized (startTimes)
        {
            startTimes.clear();
        }
    }
}
