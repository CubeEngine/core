package de.cubeisland.cubeengine.log;

import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.config.annotations.Codec;
import de.cubeisland.cubeengine.core.config.annotations.Option;
import de.cubeisland.cubeengine.log.listeners.LogListener;
import gnu.trove.map.hash.THashMap;
import java.util.EnumMap;
import java.util.Map;

@Codec("yml")
public class LogConfiguration extends Configuration
{
    public LogConfiguration()
    {
        for (LogAction action : LogAction.values())
        {
            LogListener listener = LogListener.getInstance(action.
                getListenerClass(), Log.getInstance());
            this.configs.put(listener.getConfiguration().getName(), listener.
                getConfiguration());
        }
    }

    @Override
    public void onLoaded()
    {
        //TODO register needed Listener
    }
    public Map<LogAction, String> configNames = new EnumMap<LogAction, String>(LogAction.class);
    @Option(value = "configs", genericType = Configuration.class)
    public Map<String, LogSubConfiguration> configs = new THashMap<String, LogSubConfiguration>();

    public boolean isLogging(LogAction name)
    {
        return false; //TODO
    }

    public <T extends LogSubConfiguration> T getConfiguration(LogAction action)
    {
        return (T)this.configs.get(this.configNames.get(action));
    }
}