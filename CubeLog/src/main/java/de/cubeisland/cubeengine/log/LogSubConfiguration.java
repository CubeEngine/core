package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

public abstract class LogSubConfiguration extends Configuration
{
    @Option(value = "enabled")
    public boolean enabled;
    @Option(value = "actions", genericType = Boolean.class)
    public Map<Object, Boolean> actions = new THashMap<Object, Boolean>();
    public LogListener listener;

    public LogSubConfiguration()
    {
        this.setCodec(Configuration.resolveCodec("yml"));
    }

    public abstract String getName();
}
