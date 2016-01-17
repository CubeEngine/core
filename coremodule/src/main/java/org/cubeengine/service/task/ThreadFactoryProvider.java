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
package org.cubeengine.service.task;

import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.modularity.asm.marker.Provider;
import de.cubeisland.engine.modularity.core.LifeCycle;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import org.cubeengine.module.core.CoreModule;
import org.cubeengine.service.logging.LogProvider;
import org.cubeengine.service.task.thread.CoreThreadFactory;

@Provider(ThreadFactory.class)
public class ThreadFactoryProvider implements ValueProvider<ThreadFactory>
{
    private CoreThreadFactory coreThreadFactory;

    @Inject
    public ThreadFactoryProvider(LogFactory logFactory)
    {
        this.coreThreadFactory = new CoreThreadFactory(logFactory.getLog(CoreModule.class, "CoreModule"));
    }

    @Override
    public ThreadFactory get(LifeCycle lifeCycle, Modularity modularity)
    {
        if (lifeCycle.getInformation().getClassName().equals(CoreModule.class.getName())
         || lifeCycle.getInformation().getClassName().equals(LogProvider.class.getName()))
        {
            return coreThreadFactory;
        }
        Log log = (Log)modularity.getLifecycle(Log.class).getProvided(lifeCycle);
        return new ModuleThreadFactory(coreThreadFactory.getThreadGroup(), log);
    }
}
