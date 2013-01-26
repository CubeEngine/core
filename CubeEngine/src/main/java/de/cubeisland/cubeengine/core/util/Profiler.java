package de.cubeisland.cubeengine.core.util;

import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import org.apache.commons.lang.Validate;

/**
 * This class helps profiling code
 */
public class Profiler
{
    private static final TObjectLongMap<String> startTimes = new TObjectLongHashMap<String>();

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
        Validate.notNull(id, "The ID must not be null!");
        synchronized (startTimes)
        {
            if (!startTimes.containsKey(id))
            {
                throw new IllegalStateException("This ID is not being profiled!");
            }
            return nanos - startTimes.get(id);
        }
    }

    public static long endProfiling(String id)
    {
        final long delta = System.nanoTime();
        Validate.notNull(id, "The ID must not be null!");
        synchronized (startTimes)
        {
            return delta - startTimes.remove(id);
        }
    }
}
