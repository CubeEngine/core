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
package de.cubeisland.engine.module.core.provider;

import java.util.concurrent.ThreadFactory;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.module.core.module.ModuleThreadFactory;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.service.task.thread.CoreThreadFactory;

public class ThreadFactoryProvider implements ValueProvider<ThreadFactory>
{
    private CoreThreadFactory coreThreadFactory;

    public ThreadFactoryProvider(Log log)
    {
        this.coreThreadFactory = new CoreThreadFactory(log);
    }

    @Override
    public ThreadFactory get(DependencyInformation info, Modularity modularity)
    {
        Log log = modularity.getProvider(Log.class).get(info, modularity);
        if (info.getClassName().equals(CoreModule.class.getName()))
        {
            return coreThreadFactory;
        }
        return new ModuleThreadFactory(coreThreadFactory.getThreadGroup(), log);
    }
}
