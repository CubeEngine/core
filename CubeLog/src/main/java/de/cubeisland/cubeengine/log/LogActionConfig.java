package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

public class LogActionConfig extends Configuration
{
    public LogActionConfig(boolean defaultEnabled)
    {
        this.enabled = defaultEnabled;
    }

    @Option(value = "enabled")
    public boolean                   enabled;
    @Option(value = "sub-actions")
    public Map<String, SubLogConfig> configs = new THashMap<String, SubLogConfig>();
}
