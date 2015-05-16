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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.LogTarget;
import de.cubeisland.engine.logscribe.filter.PrefixFilter;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.asm.marker.Provider;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.logging.LoggingUtil;
import de.cubeisland.engine.module.core.logging.SpongeLogFactory;
import de.cubeisland.engine.module.core.sponge.CoreModule;

@Provider(Log.class)
public class LogProvider implements ValueProvider<Log>
{
    @Inject private LogFactory logFactory;
    @Inject private FileManager fm;
    @Inject private ThreadFactory tf;
    private Map<DependencyInformation, Log> loggers = new HashMap<>();

    @Override
    public Log get(DependencyInformation info, Modularity modularity)
    {
        Log logger = loggers.get(info);
        if (logger != null)
        {
            return logger;
        }
        if (info instanceof ModuleMetadata)
        {
            String name = ((ModuleMetadata)info).getName();

            logger = logFactory.getLog(CoreModule.class, name);

            logger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(fm, name),
                                              LoggingUtil.getFileFormat(true, true), true, LoggingUtil.getCycler(), tf));

            LogTarget parentTarget = logger.addDelegate(logFactory.getLog(CoreModule.class));
            parentTarget.appendFilter(new PrefixFilter("[" + name + "] "));
        }
        else
        {
            logger = logFactory.getLog(CoreModule.class, info.getIdentifier());
            // TODO manually add Target for non-modules
        }
        return logger;
    }
}
