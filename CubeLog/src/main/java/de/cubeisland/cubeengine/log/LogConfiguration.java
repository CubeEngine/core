package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

@Codec("yml")
public class LogConfiguration extends Configuration
{
    @Option(value = "log-actions")
    public Map<String, LogActionConfig> configs = new THashMap<String, LogActionConfig>();

    public LogConfiguration()
    {
        for (LogAction action : LogAction.values())
        {
            this.configs.put(action.name(), action.getConfiguration());
        }
    }

    @Override
    public void onLoaded()
    {
        for (LogAction action : LogAction.values())
        {
            if (this.getActionConfig(action) == null)
            {
                this.configs.put(action.name(), action.getConfiguration()); // reload config if not in file
            }
            action.applyLoadedConfig(this.getActionConfig(action));
        }
    }

    public LogActionConfig getActionConfig(LogAction action)
    {
        return this.configs.get(action.name());
    }
}
