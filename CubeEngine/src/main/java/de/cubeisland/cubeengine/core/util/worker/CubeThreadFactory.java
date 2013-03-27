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
