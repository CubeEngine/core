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
package de.cubeisland.engine.module.core.sponge;

import java.util.concurrent.ThreadFactory;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogTarget;
import de.cubeisland.engine.logscribe.filter.PrefixFilter;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.logging.SpongeLogFactory;
import de.cubeisland.engine.module.core.logging.LoggingUtil;

public class LogProvider implements ValueProvider<Log>
{
    private SpongeLogFactory logFactory;

    public LogProvider(SpongeLogFactory logFactory)
    {
        this.logFactory = logFactory;
    }

    @Override
    public Log get(DependencyInformation info, Modularity modularity)
    {
        if (info instanceof ModuleMetadata)
        {
            String name = ((ModuleMetadata)info).getName();

            Log log = logFactory.getLog(CoreModule.class, name);

            /* TODO log.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(modularity.start(FileManager.class), name),
                                              LoggingUtil.getFileFormat(true, true),
                                              true, LoggingUtil.getCycler(),
                                              core.getProvided(ThreadFactory.class)));
                                              */
            LogTarget parentTarget = log.addDelegate(logFactory.getParent());

            parentTarget.appendFilter(new PrefixFilter("[" + name + "] "));

            return log;
        }
        else
        {
            return null; // TODO service Logger
        }
    }
}
