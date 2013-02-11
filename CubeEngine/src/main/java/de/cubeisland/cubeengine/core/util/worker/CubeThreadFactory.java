package de.cubeisland.cubeengine.core.util.worker;

import de.cubeisland.cubeengine.core.CubeEngine;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static de.cubeisland.cubeengine.core.logger.LogLevel.DEBUG;

public class CubeThreadFactory implements ThreadFactory
{
    private final AtomicInteger counter;
    private final ThreadGroup group;
    private static final AtomicInteger factoryCounter = new AtomicInteger(0);

    public CubeThreadFactory(String name)
    {
        this(new ThreadGroup(name));
    }

    public CubeThreadFactory(ThreadGroup group)
    {
        this.group = group;
        this.counter = new AtomicInteger(0);
        factoryCounter.incrementAndGet();
    }

    @Override
    public Thread newThread(Runnable r)
    {
        final String name = "CubeEngine #" + factoryCounter.get() + " - Thread #" + this.counter.incrementAndGet();
        CubeEngine.getLogger().log(DEBUG, "Creating thread: {0}", name);
        return new Thread(this.group, r, name);
    }
}
