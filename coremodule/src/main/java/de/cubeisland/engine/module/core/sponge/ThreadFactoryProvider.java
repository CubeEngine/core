package de.cubeisland.engine.module.core.sponge;

import java.util.concurrent.ThreadFactory;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ValueProvider;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.module.core.module.ModuleThreadFactory;
import de.cubeisland.engine.module.core.task.thread.CoreThreadFactory;

public class ThreadFactoryProvider implements ValueProvider<ThreadFactory>
{
    private CoreThreadFactory coreThreadFactory;

    @Override
    public ThreadFactory get(DependencyInformation info, Modularity modularity)
    {
        Log log = modularity.getProvider(Log.class).get(info, modularity);
        if (info.getClassName().equals(CoreModule.class.getName()))
        {
            if (coreThreadFactory == null)
            {
                coreThreadFactory = new CoreThreadFactory(log);
            }
            return coreThreadFactory;
        }
        return new ModuleThreadFactory(coreThreadFactory.getThreadGroup(), log);
    }
}
