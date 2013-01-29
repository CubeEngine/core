package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

public class LogActionConfig extends Configuration
{
    @Option("enabled")
    public boolean enabled;
    @Option("sub-actions")
    public Map<String, LoggerConfig> loggerConfigs = new THashMap<String, LoggerConfig>();
}
