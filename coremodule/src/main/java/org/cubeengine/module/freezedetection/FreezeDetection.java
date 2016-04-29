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
package org.cubeengine.module.freezedetection;


import java.io.File;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.libcube.service.task.TaskManager;

@ModuleInfo(name = "FreezeDetection", description = "Detects server freeze and produces thread dumps")
public class FreezeDetection extends Module
{
    @Inject private TaskManager taskManager;
    @Inject private Log logger;
    @Inject private ThreadFactory tf;
    @Inject File pluginFolder;

    private ScheduledExecutorService executor;
    private UUID taskId;
    private long lastHeartbeat = -1;
    private final long freezeThreshold = TimeUnit.SECONDS.toMillis(20);
    private final ConcurrentLinkedQueue<Runnable> listeners = new ConcurrentLinkedQueue<>();
    private volatile boolean freezeNotified = false;

    @Enable
    public void onEnable()
    {
        start();
        addListener(new ThreadDumpListener(logger, pluginFolder.toPath()));
    }

    @Disable
    public void onDisable()
    {
        shutdown();
    }

    public void addListener(Runnable r)
    {
        this.listeners.add(r);
    }

    public void removeListener(Runnable r)
    {
        this.listeners.remove(r);
    }

    public void start()
    {
        this.taskId = this.taskManager.runAsynchronousTimer(this, new HeartbeatLogger(), 0, 1);
        if (this.taskId == null)
        {
            throw new RuntimeException("Failed to schedule the heartbeat logging for freeze detection");
        }
        this.executor = Executors.newSingleThreadScheduledExecutor(tf);
        this.executor.scheduleAtFixedRate(new FreezeDetector(), this.freezeThreshold, this.freezeThreshold, TimeUnit.MILLISECONDS);

        this.lastHeartbeat = System.currentTimeMillis();
    }

    public void shutdown()
    {
        if (this.taskId != null)
        {
            this.taskManager.cancelTask(this, this.taskId);
            this.taskId = null;
        }
        if (this.executor != null)
        {
            this.executor.shutdown();
            try
            {
                this.executor.awaitTermination(2, TimeUnit.SECONDS);
            }
            catch (InterruptedException ignored)
            {}
            this.executor = null;
        }

        this.listeners.clear();
    }

    private class HeartbeatLogger implements Runnable
    {
        @Override
        public void run()
        {
            lastHeartbeat = System.currentTimeMillis();
            freezeNotified = false;
        }
    }

    private class FreezeDetector implements Runnable
    {
        @Override
        public void run()
        {
            if (System.currentTimeMillis() - lastHeartbeat > freezeThreshold && !freezeNotified)
            {
                freezeNotified = true;
                listeners.forEach(Runnable::run);
            }
        }
    }
}
