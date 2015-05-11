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
            Log log = logFactory.getLog(info.getIdentifier(), name);
            log.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(modularity.start(FileManager.class), name),
                                              LoggingUtil.getFileFormat(true, true),
                                              true, LoggingUtil.getCycler(),
                                              core.getProvided(ThreadFactory.class)));
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
